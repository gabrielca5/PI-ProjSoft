package com.example.cursos.controller;

import com.example.cursos.dto.TransacaoRequestDTO;
import com.example.cursos.model.Transacao;
import com.example.cursos.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transacao")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @GetMapping
    public ResponseEntity<List<Transacao>> listar(@RequestParam(required = false) Long clienteId) {
        return ResponseEntity.ok(transacaoService.listar(clienteId));
    }

    @PostMapping
    public ResponseEntity<Transacao> criar(@Valid @RequestBody TransacaoRequestDTO dto) {
        Transacao criada = transacaoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        transacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
