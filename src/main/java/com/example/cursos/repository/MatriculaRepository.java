package com.example.cursos.repository;

import com.example.cursos.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    List<Matricula> findByCursoId(Long cursoId);
}
