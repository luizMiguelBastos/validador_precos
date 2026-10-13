package com.example.validador_precos.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ExplicacaoServiceTest {

    @Test
    @DisplayName("Se a IA falhar, devolve uma mensagem padrão em vez de quebrar a validação")
    void deveDevolverMensagemPadraoQuandoIaFalhar() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenThrow(new RuntimeException("Ollama fora do ar"));

        ExplicacaoService explicacaoService = new ExplicacaoService(builder);

        String explicacao = explicacaoService.explicar("ABS", new BigDecimal("1.00"),
                new BigDecimal("1.08"), new BigDecimal("5.40"), false);

        assertEquals("Explicação indisponível no momento. Resultado: bloqueado.", explicacao);
    }
}
