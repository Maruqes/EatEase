package com.eatease.eatease.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "item")

public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nome;
    private long tipoPrato_id;
    private float preco;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String ingredientesJson; // Stores JSON array of {ingrediente_id: X, quantidade: Y}

    private boolean eComposto;
    private int stockAtual;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public long getTipoPrato_id() {
        return tipoPrato_id;
    }

    public void setTipoPrato_id(long tipoPrato_id) {
        this.tipoPrato_id = tipoPrato_id;
    }

    public float getPreco() {
        return preco;
    }

    public void setPreco(float preco) {
        this.preco = preco;
    }

    public String getIngredientesJson() {
        return ingredientesJson;
    }

    public void setIngredientesJson(String ingredientesJson) {
        this.ingredientesJson = ingredientesJson;
    }

    public boolean iseComposto() {
        return eComposto;
    }

    public void seteComposto(boolean eComposto) {
        this.eComposto = eComposto;
    }

    public int getStockAtual() {
        return stockAtual;
    }

    public void setStockAtual(int stockAtual) {
        this.stockAtual = stockAtual;
    }

}
