package com.example.validador_precos.service;

import com.example.validador_precos.dtos.ValidacaoReponse;
import com.example.validador_precos.models.Produto;
import com.example.validador_precos.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidadorServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CotacaoService cotacaoService;

    @Mock
    private ExplicacaoService explicacaoService;

    @InjectMocks
    private ValidadorService validadorService;

    private Produto abs;

    @BeforeEach
    void setUp() {
        abs = new Produto();
        abs.setNome("ABS");
        abs.setPesoKg(new BigDecimal("0.05"));
        abs.setPrecoMateriaPrimaUsdKg(new BigDecimal("2.00"));
        abs.setOutrosCustos(new BigDecimal("0.40"));
    }

    private void prepararCenario(String cotacao) {
        when(produtoRepository.findByNome("ABS")).thenReturn(Optional.of(abs));
        when(cotacaoService.buscarCotacaoDolar()).thenReturn(new BigDecimal(cotacao));
        when(explicacaoService.explicar(anyString(), any(), any(), any(), anyBoolean()))
                .thenReturn("explicação de teste");
    }

    @Test
    @DisplayName("Calcula o preço mínimo com dólar a 5.40: (0.05 x 2.00 x 5.40 + 0.40) x 1.15 = 1.08")
    void deveCalcularPrecoMinimoComDolarFixo() {
        BigDecimal precoMinimo = validadorService.calcularPrecoMinimo(abs, new BigDecimal("5.40"));

        assertEquals(new BigDecimal("1.08"), precoMinimo);
    }

    @Test
    @DisplayName("Arredonda o preço mínimo para 2 casas decimais")
    void deveArredondarPrecoMinimoParaDuasCasas() {
        BigDecimal precoMinimo = validadorService.calcularPrecoMinimo(abs, new BigDecimal("5.0096"));

        assertEquals(new BigDecimal("1.04"), precoMinimo);
        assertEquals(2, precoMinimo.scale());
    }

    @Test
    @DisplayName("Aprova quando o preço proposto é maior que o mínimo")
    void deveAprovarPrecoAcimaDoMinimo() {
        prepararCenario("5.40");

        ValidacaoReponse resposta = validadorService.validar("ABS", new BigDecimal("1.50"));

        assertTrue(resposta.aprovado());
        assertEquals(new BigDecimal("1.08"), resposta.valorMinimo());
        assertEquals(new BigDecimal("5.40"), resposta.cotacaoDolar());
        assertEquals("explicação de teste", resposta.explicacao());
    }

    @Test
    @DisplayName("Aprova quando o preço proposto é exatamente igual ao mínimo")
    void deveAprovarPrecoIgualAoMinimo() {
        prepararCenario("5.40");

        ValidacaoReponse resposta = validadorService.validar("ABS", new BigDecimal("1.08"));

        assertTrue(resposta.aprovado());
    }

    @Test
    @DisplayName("Bloqueia quando o preço proposto é menor que o mínimo")
    void deveBloquearPrecoAbaixoDoMinimo() {
        prepararCenario("5.40");

        ValidacaoReponse resposta = validadorService.validar("ABS", new BigDecimal("1.00"));

        assertFalse(resposta.aprovado());
        assertEquals(new BigDecimal("1.08"), resposta.valorMinimo());
    }

    @Test
    @DisplayName("Busca o dólar uma única vez por validação")
    void deveBuscarCotacaoUmaVezPorValidacao() {
        prepararCenario("5.40");

        validadorService.validar("ABS", new BigDecimal("1.50"));

        verify(cotacaoService, times(1)).buscarCotacaoDolar();
    }

    @Test
    @DisplayName("Lança 404 quando o produto não existe e não chama dólar nem IA")
    void deveLancar404QuandoProdutoNaoExiste() {
        when(produtoRepository.findByNome("PVC")).thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> validadorService.validar("PVC", new BigDecimal("1.00")));

        assertEquals(404, erro.getStatusCode().value());
        verifyNoInteractions(cotacaoService, explicacaoService);
    }
}
