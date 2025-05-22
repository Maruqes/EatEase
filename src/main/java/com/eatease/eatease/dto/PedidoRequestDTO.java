package com.eatease.eatease.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for receiving pedido creation requests
 */
public class PedidoRequestDTO {

    @NotNull(message = "O ID do prato é obrigatório")
    private List<Long> itensIds;

    @NotNull(message = "O ID da mesa é obrigatório")
    @Min(value = 1, message = "O ID da mesa deve ser positivo")
    private Long mesaId;

    @NotNull(message = "O ID do funcionário é obrigatório")
    @Min(value = 1, message = "O ID do funcionário deve ser positivo")
    private Long funcionarioId;

    @Valid
    private List<Long> ingredientesRemover;

    private String observacao;

    // Constructors
    public PedidoRequestDTO() {
    }

    public PedidoRequestDTO(List<Long> itensIds, Long mesaId, Long funcionarioId, String observacao) {
        this.itensIds = itensIds;
        this.mesaId = mesaId;
        this.funcionarioId = funcionarioId;
        this.observacao = observacao;
    }

    // Getters and Setters
    public List<Long> getItensIds() {
        return itensIds;
    }

    public void setItensIds(List<Long> itensIds) {
        this.itensIds = itensIds;
    }

    public Long getMesaId() {
        return mesaId;
    }

    public void setMesaId(Long mesaId) {
        this.mesaId = mesaId;
    }

    public Long getFuncionarioId() {
        return funcionarioId;
    }

    public void setFuncionarioId(Long funcionarioId) {
        this.funcionarioId = funcionarioId;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public List<Long> getIngredientesRemover() {
        return ingredientesRemover;
    }

    public void setIngredientesRemover(List<Long> ingredientes) {
        this.ingredientesRemover = ingredientes;
    }
}