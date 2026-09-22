package com.example.cursos.controller;

import com.example.cursos.dto.MateriaRequestDTO;
import com.example.cursos.model.Materia;
import com.example.cursos.service.MateriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/materias")
public class MateriaController {

    private final MateriaService materiaService;

    public MateriaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    @GetMapping
    public ResponseEntity<List<Materia>> listar(@RequestParam Long cursoId) {
        return ResponseEntity.ok(materiaService.listarPorCurso(cursoId));
    }

    @PostMapping
    public ResponseEntity<Materia> criar(@Valid @RequestBody MateriaRequestDTO dto) {
        Materia criada = materiaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }
}
