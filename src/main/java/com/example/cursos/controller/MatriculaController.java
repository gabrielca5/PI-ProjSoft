package com.example.cursos.controller;

import com.example.cursos.dto.MatriculaRequestDTO;
import com.example.cursos.model.Matricula;
import com.example.cursos.service.MatriculaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matriculas")
public class MatriculaController {

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    @GetMapping
    public ResponseEntity<List<Matricula>> listar(@RequestParam Long cursoId) {
        return ResponseEntity.ok(matriculaService.listarPorCurso(cursoId));
    }

    @PostMapping
    public ResponseEntity<Matricula> matricular(@Valid @RequestBody MatriculaRequestDTO dto) {
        Matricula criada = matriculaService.matricular(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }
}
