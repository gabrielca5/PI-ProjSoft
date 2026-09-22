package com.example.cursos.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class CursoRequestDTO {

    @NotBlank(message = "nome é obrigatório")
    private String nome;

    private String descricao;

    private Integer cargaHoraria;

    private BigDecimal preco;

    public CursoRequestDTO() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(Integer cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }
}
