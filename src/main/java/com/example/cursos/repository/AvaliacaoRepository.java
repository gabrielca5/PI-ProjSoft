package com.example.cursos.repository;

import com.example.cursos.model.Avaliacao;
import com.example.cursos.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
}
