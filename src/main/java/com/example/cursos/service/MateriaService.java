package com.example.cursos.service;

import com.example.cursos.dto.MateriaRequestDTO;
import com.example.cursos.exception.RecursoNotFoundException;
import com.example.cursos.model.Curso;
import com.example.cursos.model.Materia;
import com.example.cursos.model.Professor;
import com.example.cursos.repository.CursoRepository;
import com.example.cursos.repository.MateriaRepository;
import com.example.cursos.repository.ProfessorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MateriaService {

    private final MateriaRepository materiaRepository;
    private final CursoRepository cursoRepository;
    private final ProfessorRepository professorRepository;

    public MateriaService(MateriaRepository materiaRepository, CursoRepository cursoRepository, ProfessorRepository professorRepository) {
        this.materiaRepository = materiaRepository;
        this.cursoRepository = cursoRepository;
        this.professorRepository = professorRepository;
    }

    public List<Materia> listarPorCurso(Long cursoId) {
        return materiaRepository.findByCursoId(cursoId);
    }

    public Materia criar(MateriaRequestDTO dto) {
        Curso curso = cursoRepository.findById(dto.getCursoId())
                .orElseThrow(() -> new RecursoNotFoundException("Curso", dto.getCursoId()));
        Professor professor = professorRepository.findById(dto.getProfessorId())
                .orElseThrow(() -> new RecursoNotFoundException("Professor", dto.getProfessorId()));

        Materia materia = new Materia(dto.getNome(), dto.getCargaHoraria(), professor, curso);
        return materiaRepository.save(materia);
    }
}
