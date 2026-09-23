package com.example.cursos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransacaoRequestDTO {

    @NotNull(message = "clienteId é obrigatório")
    private Long clienteId;

    @NotBlank(message = "codigoAcao é obrigatório")
    private String codigoAcao;

    @NotNull(message = "quantidade é obrigatória")
    @Positive(message = "quantidade deve ser positiva")
    private Integer quantidade;

    @NotNull(message = "precoUnitario é obrigatório")
    @Positive(message = "precoUnitario deve ser positivo")
    private BigDecimal precoUnitario;

    @NotNull(message = "dataTransacao é obrigatória")
    private LocalDate dataTransacao;

    public TransacaoRequestDTO() {
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getCodigoAcao() {
        return codigoAcao;
    }

    public void setCodigoAcao(String codigoAcao) {
        this.codigoAcao = codigoAcao;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public LocalDate getDataTransacao() {
        return dataTransacao;
    }

    public void setDataTransacao(LocalDate dataTransacao) {
        this.dataTransacao = dataTransacao;
    }
}
