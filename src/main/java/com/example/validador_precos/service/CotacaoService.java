package com.example.validador_precos.service;

import com.example.validador_precos.dtos.CotacaoDolar;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Service
public class CotacaoService {

    private static final String URL_DOLAR_BCB =
            "https://api.bcb.gov.br/dados/serie/bcdata.sgs.1/dados/ultimos/1?formato=json";

    private final RestClient restClient = RestClient.create();

    public BigDecimal buscarCotacaoDolar() {
        CotacaoDolar[] cotacoes = restClient.get()
                .uri(URL_DOLAR_BCB)
                .retrieve()
                .body(CotacaoDolar[].class);

        return new BigDecimal(cotacoes[0].valor());
    }
}
