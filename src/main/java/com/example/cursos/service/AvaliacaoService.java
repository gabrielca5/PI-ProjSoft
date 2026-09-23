package com.example.cursos.service;

import com.example.cursos.dto.AvaliacaoRequestDTO;
import com.example.cursos.dto.UsuarioResponseDTO;
import com.example.cursos.exception.RecursoNotFoundException;
import com.example.cursos.model.Avaliacao;
import com.example.cursos.repository.AvaliacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvaliacaoService {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    public AvaliacaoService(AvaliacaoRepository AvaliacaoRepository) {
        this.avaliacaoRepository = AvaliacaoRepository;
    }

    public List<Avaliacao> listar() {

        return avaliacaoRepository.findAll();
    }

    public Avaliacao criar(AvaliacaoRequestDTO dto) {

        Avaliacao avaliacao = new Avaliacao(
                dto.getAutor(),
                dto.getConteudo(),
                dto.getNota(),
                dto.getDataAvaliacao()
        );

        return avaliacaoRepository.save(avaliacao);

    }}
