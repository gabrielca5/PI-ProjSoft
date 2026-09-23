package com.example.cursos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "avaliacoes")
public class Avaliacao {

    public Avaliacao() {
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "autor", nullable = false)
    private String autor;

    @Column(name = "conteudo", nullable = false)
    private String conteudo;

    @Column(name = "nota", nullable = false)
    private Short nota;


    @Column(name = "data_Avaliacao", nullable = false)
    private LocalDate dataAvaliacao;

    public Avaliacao(String autor, String conteudo, Short nota, LocalDate dataAvaliacao) {
        this.autor = autor;
        this.conteudo = conteudo;
        this.nota = nota;
        this.dataAvaliacao = dataAvaliacao;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public LocalDate getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(LocalDate dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }

    public Short getNota() {
        return nota;
    }

    public void setNota(Short nota) {
        this.nota = nota;
    }
}
