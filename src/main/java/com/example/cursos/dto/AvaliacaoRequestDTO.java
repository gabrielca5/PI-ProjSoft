package com.example.cursos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AvaliacaoRequestDTO {

    @NotNull(message = "autor é obrigatório")
    private String autor;

    @NotNull(message = "nota é obrigatório")
    private Short nota;

    @NotNull(message = "Conteudo é obrigatório")
    private String conteudo;


    @NotNull(message = "dataAvaliacao é obrigatória")
    private LocalDate dataAvaliacao;

    public AvaliacaoRequestDTO() {
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

    public void setNota(Short nota){
        this.nota = nota ;
    }

    public Short getNota() {
        return nota;
    }

    public LocalDate getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(LocalDate dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }
}
