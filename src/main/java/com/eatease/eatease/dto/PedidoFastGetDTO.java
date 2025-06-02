package com.eatease.eatease.dto;

import java.util.List;

import com.eatease.eatease.model.Item;

public class PedidoFastGetDTO {
    private long id;

    private List<Item> itensIds;
    private long estadoPedido_id;
    private long mesa_number;
    private String funcionario;
    private String dataHora;
    private String observacao;
    private List<Long> ingredientesRemover;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Item> getItensIds() {
        return itensIds;
    }

    public void setItensIds(List<Item> itensIds) {
        this.itensIds = itensIds;
    }

    public long getEstadoPedido_id() {
        return estadoPedido_id;
    }

    public void setEstadoPedido_id(long estadoPedido_id) {
        this.estadoPedido_id = estadoPedido_id;
    }

    public long getMesa_number() {
        return mesa_number;
    }

    public void setMesa_number(long mesa_number) {
        this.mesa_number = mesa_number;
    }

    public String getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(String funcionario) {
        this.funcionario = funcionario;
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
