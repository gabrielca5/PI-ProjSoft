package com.example.cursos.service;

import com.example.cursos.dto.ProfessorRequestDTO;
import com.example.cursos.model.Professor;
import com.example.cursos.repository.ProfessorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfessorService {

    private final ProfessorRepository professorRepository;

    public ProfessorService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    public List<Professor> listar() {
        return professorRepository.findAll();
    }

    public Professor criar(ProfessorRequestDTO dto) {
        Professor professor = new Professor(dto.getNome(), dto.getEmail(), dto.getEspecialidade());
        return professorRepository.save(professor);
    }
}
