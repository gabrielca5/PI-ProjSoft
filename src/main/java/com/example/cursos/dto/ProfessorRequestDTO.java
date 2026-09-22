package com.example.cursos.dto;

import jakarta.validation.constraints.NotBlank;

public class ProfessorRequestDTO {

    @NotBlank(message = "nome é obrigatório")
    private String nome;

    @NotBlank(message = "email é obrigatório")
    private String email;

    private String especialidade;

    public ProfessorRequestDTO() {
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

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }
}
