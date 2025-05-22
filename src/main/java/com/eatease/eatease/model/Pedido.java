package com.eatease.eatease.model;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "pedido")

public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private List<Long> itensIds;
    private long estadoPedido_id;
    private long mesa_id;
    private long funcionario_id;
    private String dataHora;
    private String observacao;
    private List<Long> ingredientesRemover;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Long> getItensIds() {
        return itensIds;
    }

    public void setItensIds(List<Long> itensIds) {
        this.itensIds = itensIds;
    }

    public long getEstadoPedido_id() {
        return estadoPedido_id;
    }

    public void setEstadoPedido_id(long estadoPedido_id) {
        this.estadoPedido_id = estadoPedido_id;
    }

    public long getMesa_id() {
        return mesa_id;
    }

    public void setMesa_id(long mesa_id) {
        this.mesa_id = mesa_id;
    }

    public long getFuncionario_id() {
        return funcionario_id;
    }

    public void setFuncionario_id(long funcionario_id) {
        this.funcionario_id = funcionario_id;
    }

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
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

    public void setIngredientesRemover(List<Long> ingredientesRemover) {
        this.ingredientesRemover = ingredientesRemover;
    }
}
