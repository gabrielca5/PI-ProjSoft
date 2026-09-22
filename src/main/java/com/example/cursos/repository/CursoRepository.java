package com.example.cursos.repository;

import com.example.cursos.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    // filtro "startWith": nome começa com a string enviada, ignorando deletados
    List<Curso> findByDeletadoFalseAndNomeStartingIgnoreCase(String nome);

    List<Curso> findByDeletadoFalse();
}
