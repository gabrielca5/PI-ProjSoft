package com.example.cursos.controller;

import com.example.cursos.dto.ProfessorRequestDTO;
import com.example.cursos.model.Professor;
import com.example.cursos.service.ProfessorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/professores")
public class ProfessorController {

    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @GetMapping
    public ResponseEntity<List<Professor>> listar() {
        return ResponseEntity.ok(professorService.listar());
    }

    @PostMapping
    public ResponseEntity<Professor> criar(@Valid @RequestBody ProfessorRequestDTO dto) {
        Professor criado = professorService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }
}
