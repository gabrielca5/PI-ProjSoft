package com.example.cursos.exception;

public class CursoNotFoundException extends RecursoNotFoundException {
    public CursoNotFoundException(Long id) {
        super("Curso", id);
    }
}
