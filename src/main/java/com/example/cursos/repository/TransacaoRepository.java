package com.example.cursos.repository;

import com.example.cursos.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByClienteId(Long clienteId);
}
