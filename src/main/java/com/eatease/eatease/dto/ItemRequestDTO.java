// src/main/java/com/eatease/eatease/dto/ItemRequestDTO.java
package com.eatease.eatease.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ItemRequestDTO {

    @NotBlank(message = "O nome não pode estar vazio")
    private String nome;

    @NotNull(message = "O ID do tipo de prato é obrigatório")
    private Long tipoPratoId;

    @NotNull(message = "O preço é obrigatório")
    @Min(value = 0, message = "O preço não pode ser negativo")
    private Float preco;

    @NotNull(message = "A lista de ingredientes é obrigatória")
    @Valid
    private List<IngredienteQuantDTO> ingredientes;

    private boolean composto;

    /* getters & setters – usa sempre WRAPPERS (Long/Float/Integer) */
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getTipoPratoId() {
        return tipoPratoId;
    }

    public void setTipoPratoId(Long tipoPratoId) {
        this.tipoPratoId = tipoPratoId;
    }

    public Float getPreco() {
        return preco;
    }

    public void setPreco(Float preco) {
        this.preco = preco;
    }

    public List<IngredienteQuantDTO> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<IngredienteQuantDTO> ingredientes) {
        this.ingredientes = ingredientes;
    }

    public boolean isComposto() {
        return composto;
    }

    public void setComposto(boolean composto) {
        this.composto = composto;
    }

}
