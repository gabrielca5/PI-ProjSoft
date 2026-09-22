package com.example.cursos.exception;

public class CursoNotFoundException extends RuntimeException {
    public CursoNotFoundException(Long id) {
        super("Curso não encontrado: " + id);
    }
}
