package com.example.cursos.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PROFESSOR")
public class Professor extends Pessoa {

    private String especialidade;

    public Professor() {
    }

    public Professor(String nome, String email, String especialidade) {
        super(nome, email);
        this.especialidade = especialidade;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }
}
