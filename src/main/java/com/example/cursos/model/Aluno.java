package com.example.cursos.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ALUNO")
public class Aluno extends Pessoa {

    private String matricula;

    public Aluno() {
    }

    public Aluno(String nome, String email, String matricula) {
        super(nome, email);
        this.matricula = matricula;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }
}
