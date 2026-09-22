package com.example.cursos.dto;

import jakarta.validation.constraints.NotNull;

public class MatriculaRequestDTO {

    @NotNull(message = "cursoId é obrigatório")
    private Long cursoId;

    @NotNull(message = "alunoId é obrigatório")
    private Long alunoId;

    public MatriculaRequestDTO() {
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public Long getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }
}
