package com.example.cursos.controller;

import com.example.cursos.dto.AlunoRequestDTO;
import com.example.cursos.model.Aluno;
import com.example.cursos.service.AlunoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @GetMapping
    public ResponseEntity<List<Aluno>> listar() {
        return ResponseEntity.ok(alunoService.listar());
    }

    @PostMapping
    public ResponseEntity<Aluno> criar(@Valid @RequestBody AlunoRequestDTO dto) {
        Aluno criado = alunoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }
}
