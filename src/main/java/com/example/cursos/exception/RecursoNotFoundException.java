package com.example.cursos.exception;

public class RecursoNotFoundException extends RuntimeException {
    public RecursoNotFoundException(String recurso, Long id) {
        super(recurso + " não encontrado: " + id);
    }
}
