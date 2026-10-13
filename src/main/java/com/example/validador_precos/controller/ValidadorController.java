package com.example.validador_precos.controller;

import com.example.validador_precos.dtos.ValidacaoReponse;
import com.example.validador_precos.dtos.ValidacaoRequest;
import com.example.validador_precos.service.ValidadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ValidadorController {

    private ValidadorService validadorService;
    public ValidadorController(ValidadorService validadorService) {
        this.validadorService = validadorService;
    }

    @PostMapping("/validar")
    public ResponseEntity<ValidacaoReponse> validar(@RequestBody @Valid ValidacaoRequest validacaoRequest) {
        ValidacaoReponse validacaoReponse = validadorService.validar(validacaoRequest.nome(),validacaoRequest.valorProposto());
        return ResponseEntity.ok(validacaoReponse);
    }



}
