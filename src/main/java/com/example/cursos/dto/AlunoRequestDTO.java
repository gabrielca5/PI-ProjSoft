package com.example.cursos.dto;

import jakarta.validation.constraints.NotBlank;

public class AlunoRequestDTO {

    @NotBlank(message = "nome é obrigatório")
    private String nome;

    @NotBlank(message = "email é obrigatório")
    private String email;

    private String matricula;

    public AlunoRequestDTO() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }
}
