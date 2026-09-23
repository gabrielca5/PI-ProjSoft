package com.example.cursos.controller;

import com.example.cursos.dto.AvaliacaoRequestDTO;
import com.example.cursos.model.Avaliacao;
import com.example.cursos.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avaliacao")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService AvaliacaoService) {
        this.avaliacaoService = AvaliacaoService;
    }

    @GetMapping
    public ResponseEntity<List<Avaliacao>> listar(@RequestParam(required = false) Long usuarioId) {
        // Exemplo de lógica interna
        List<Avaliacao> avaliacoes = avaliacaoService.listar();

        return ResponseEntity.ok(avaliacoes);
    }

    @PostMapping
    public ResponseEntity<Avaliacao> criar(@Valid @RequestBody AvaliacaoRequestDTO dto) {
        Avaliacao criada = avaliacaoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }


}
