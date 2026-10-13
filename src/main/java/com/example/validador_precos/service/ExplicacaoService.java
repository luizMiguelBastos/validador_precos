package com.example.validador_precos.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ExplicacaoService {

    private static final String SYSTEM_PROMPT = """
            Você é um assistente comercial de uma indústria de plásticos.
            Sua função é explicar ao vendedor, em português simples, o resultado de uma validação de preço.
            Regras:
            - Use somente os números informados. Nunca calcule nem invente valores.
            - Responda em no máximo 2 frases curtas.
            - Se o preço foi bloqueado, diga quanto falta para o preço mínimo e sugira negociar ou pedir aprovação do gerente.
            - Se foi aprovado, confirme de forma direta.
            """;

    private final ChatClient chatClient;

    public ExplicacaoService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
    }

    public String explicar(String nomeProduto, BigDecimal valorProposto, BigDecimal precoMinimo,
                           BigDecimal cotacaoDolar, boolean aprovado) {
        // Os números são calculados aqui, no código. A IA só transforma em texto.
        BigDecimal diferenca = valorProposto.subtract(precoMinimo);
        BigDecimal percentual = diferenca
                .multiply(new BigDecimal("100"))
                .divide(precoMinimo, 1, RoundingMode.HALF_UP);

        String dados = """
                Produto: %s
                Preço proposto: R$ %s
                Preço mínimo: R$ %s
                Diferença: R$ %s (%s%%)
                Cotação do dólar usada: R$ %s
                Resultado: %s
                """.formatted(nomeProduto, valorProposto, precoMinimo, diferenca, percentual,
                cotacaoDolar, aprovado ? "APROVADO" : "BLOQUEADO");

        try {
            return chatClient.prompt()
                    .user(dados)
                    .call()
                    .content();
        } catch (Exception e) {
            // Se a IA estiver fora do ar, a validação continua funcionando.
            return "Explicação indisponível no momento. Resultado: " + (aprovado ? "aprovado." : "bloqueado.");
        }
    }
}
