package com.example.validador_precos.repository;

import com.example.validador_precos.models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
    public Optional<Produto> findByNome(String nome);
}
