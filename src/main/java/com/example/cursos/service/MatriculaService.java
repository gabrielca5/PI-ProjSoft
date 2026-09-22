package com.example.cursos.service;

import com.example.cursos.dto.MatriculaRequestDTO;
import com.example.cursos.exception.RecursoNotFoundException;
import com.example.cursos.model.Aluno;
import com.example.cursos.model.Curso;
import com.example.cursos.model.Matricula;
import com.example.cursos.repository.AlunoRepository;
import com.example.cursos.repository.CursoRepository;
import com.example.cursos.repository.MatriculaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final CursoRepository cursoRepository;
    private final AlunoRepository alunoRepository;

    public MatriculaService(MatriculaRepository matriculaRepository, CursoRepository cursoRepository, AlunoRepository alunoRepository) {
        this.matriculaRepository = matriculaRepository;
        this.cursoRepository = cursoRepository;
        this.alunoRepository = alunoRepository;
    }

    public List<Matricula> listarPorCurso(Long cursoId) {
        return matriculaRepository.findByCursoId(cursoId);
    }

    public Matricula matricular(MatriculaRequestDTO dto) {
        Curso curso = cursoRepository.findById(dto.getCursoId())
                .orElseThrow(() -> new RecursoNotFoundException("Curso", dto.getCursoId()));
        Aluno aluno = alunoRepository.findById(dto.getAlunoId())
                .orElseThrow(() -> new RecursoNotFoundException("Aluno", dto.getAlunoId()));

        return matriculaRepository.save(new Matricula(curso, aluno));
    }
}
