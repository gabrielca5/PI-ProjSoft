package com.example.cursos.service;

import com.example.cursos.dto.AlunoRequestDTO;
import com.example.cursos.model.Aluno;
import com.example.cursos.repository.AlunoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public List<Aluno> listar() {
        return alunoRepository.findAll();
    }

    public Aluno criar(AlunoRequestDTO dto) {
        Aluno aluno = new Aluno(dto.getNome(), dto.getEmail(), dto.getMatricula());
        return alunoRepository.save(aluno);
    }
}
