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
    public ValidadorService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public Produto buscarProduto(String nome) {
        return produtoRepository.findByNome(nome).orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404)));
    }

    public BigDecimal calcularPrecoMinimo(Produto produto){
        BigDecimal custoDoPlastico = produto.getPesoKg().multiply(produto.getPrecoMateriaPrimaUsdKg())
                .multiply(new BigDecimal("5.40"));
        BigDecimal custoTotal = custoDoPlastico.add(produto.getOutrosCustos());
        BigDecimal precoMinimo = custoTotal.multiply(new BigDecimal("1.15"));
        return precoMinimo.setScale(2, RoundingMode.HALF_UP);
    }

    public ValidacaoReponse validar(String nome, BigDecimal valorProposto){
        Produto produto = buscarProduto(nome);
        BigDecimal precoMinimo = calcularPrecoMinimo(produto);
        if (valorProposto.compareTo(precoMinimo) >= 0){
            return new ValidacaoReponse(produto.getNome(),valorProposto,precoMinimo,true);
        }
        return new ValidacaoReponse(produto.getNome(),valorProposto,precoMinimo,false);
    }





}
