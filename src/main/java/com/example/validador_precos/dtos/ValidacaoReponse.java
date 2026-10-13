package com.example.validador_precos.dtos;

import java.math.BigDecimal;

public record ValidacaoReponse(
        String nomeproduto,
        BigDecimal valorProposto,
        BigDecimal valorMinimo,
        BigDecimal cotacaoDolar,
        Boolean aprovado
) {
}
