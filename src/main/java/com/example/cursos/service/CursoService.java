package com.example.cursos.service;

import com.example.cursos.dto.CursoRequestDTO;
import com.example.cursos.exception.CursoNotFoundException;
import com.example.cursos.model.Curso;
import com.example.cursos.repository.CursoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    public List<Curso> listar(String nome) {
        if (nome == null || nome.isBlank()) {
            return cursoRepository.findByDeletadoFalse();
        }
        return cursoRepository.findByDeletadoFalseAndNomeStartingIgnoreCase(nome);
    }

    public Curso criar(CursoRequestDTO dto) {
        Curso curso = new Curso(dto.getNome(), dto.getDescricao(), dto.getCargaHoraria(), dto.getPreco());
        return cursoRepository.save(curso);
    }

    public void deletar(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new CursoNotFoundException(id));
        curso.setDeletado(true);
        cursoRepository.save(curso);
    }
}
