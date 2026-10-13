package com.example.validador_precos.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ValidacaoRequest(
        @NotBlank String nome,
        @NotNull @Positive BigDecimal valorProposto) {
}
