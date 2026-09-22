package com.example.cursos.repository;

import com.example.cursos.model.Materia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MateriaRepository extends JpaRepository<Materia, Long> {

    List<Materia> findByCursoId(Long cursoId);
}
