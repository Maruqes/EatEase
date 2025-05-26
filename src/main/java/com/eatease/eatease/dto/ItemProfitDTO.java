package com.eatease.eatease.dto;

import java.math.BigDecimal;

/**
 * DTO for returning item profit information with quantity and profit
 */
public class ItemProfitDTO {
    private int quantidade;
    private BigDecimal lucro;

    // Constructors
    public ItemProfitDTO() {
    }

    public ItemProfitDTO(int quantidade, BigDecimal lucro) {
        this.quantidade = quantidade;
        this.lucro = lucro;
    }

    // Getters and Setters
    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getLucro() {
        return lucro;
    }

    public void setLucro(BigDecimal lucro) {
        this.lucro = lucro;
    }
}
