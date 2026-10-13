package com.example.validador_precos.service;

import com.example.validador_precos.dtos.ValidacaoReponse;
import com.example.validador_precos.models.Produto;
import com.example.validador_precos.repository.ProdutoRepository;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ValidadorService {

    private ProdutoRepository produtoRepository;
    private CotacaoService cotacaoService;
    private ExplicacaoService explicacaoService;
    public ValidadorService(ProdutoRepository produtoRepository, CotacaoService cotacaoService,
                            ExplicacaoService explicacaoService) {
        this.produtoRepository = produtoRepository;
        this.cotacaoService = cotacaoService;
        this.explicacaoService = explicacaoService;
    }

    public Produto buscarProduto(String nome) {
        return produtoRepository.findByNome(nome).orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404)));
    }

    public BigDecimal calcularPrecoMinimo(Produto produto, BigDecimal cotacaoDolar){
        BigDecimal custoDoPlastico = produto.getPesoKg().multiply(produto.getPrecoMateriaPrimaUsdKg())
                .multiply(cotacaoDolar);
        BigDecimal custoTotal = custoDoPlastico.add(produto.getOutrosCustos());
        BigDecimal precoMinimo = custoTotal.multiply(new BigDecimal("1.15"));
        return precoMinimo.setScale(2, RoundingMode.HALF_UP);
    }

    public ValidacaoReponse validar(String nome, BigDecimal valorProposto){
        Produto produto = buscarProduto(nome);
        BigDecimal cotacaoDolar = cotacaoService.buscarCotacaoDolar();
        BigDecimal precoMinimo = calcularPrecoMinimo(produto, cotacaoDolar);
        boolean aprovado = valorProposto.compareTo(precoMinimo) >= 0;
        String explicacao = explicacaoService.explicar(produto.getNome(), valorProposto, precoMinimo, cotacaoDolar, aprovado);
        return new ValidacaoReponse(produto.getNome(), valorProposto, precoMinimo, cotacaoDolar, aprovado, explicacao);
    }





}
