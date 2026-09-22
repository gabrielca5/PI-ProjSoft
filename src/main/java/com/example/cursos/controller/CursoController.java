package com.example.cursos.controller;

import com.example.cursos.dto.CursoRequestDTO;
import com.example.cursos.model.Curso;
import com.example.cursos.service.CursoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    public ResponseEntity<List<Curso>> listar(@RequestParam(required = false) String nome) {
        return ResponseEntity.ok(cursoService.listar(nome));
    }

    @PostMapping
    public ResponseEntity<Curso> criar(@Valid @RequestBody CursoRequestDTO dto) {
        Curso criado = cursoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    // Esta é a rota "extra" que na prova pode ser criada via Pull Request
    // (branch separada -> PR -> pipeline de testes roda -> merge)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        cursoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
