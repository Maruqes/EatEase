package com.eatease.eatease.dto;

import java.math.BigDecimal;

public class DashBoardDTO {
    private BigDecimal vendasDia; // Total faturado hoje em €
    private Integer numeroPedidos; // Número de pedidos hoje
    private BigDecimal ticketMedio; // Valor médio por pedido
    private BigDecimal percentualMudanca; // Percentual de mudança comparado com ontem
    private String setinha; // "↑" ou "↓"
    private String cor; // "green" ou "red"
    private BigDecimal vendasOntem; // Total de vendas de ontem
    private Integer pedidosOntem; // Número de pedidos de ontem

    public DashBoardDTO() {
    }

    public DashBoardDTO(BigDecimal vendasDia, Integer numeroPedidos, BigDecimal ticketMedio,
            BigDecimal percentualMudanca, String setinha, String cor) {
        this.vendasDia = vendasDia;
        this.numeroPedidos = numeroPedidos;
        this.ticketMedio = ticketMedio;
        this.percentualMudanca = percentualMudanca;
        this.setinha = setinha;
        this.cor = cor;
    }

    // Getters e Setters
    public BigDecimal getVendasDia() {
        return vendasDia;
    }

    public void setVendasDia(BigDecimal vendasDia) {
        this.vendasDia = vendasDia;
    }

    public Integer getNumeroPedidos() {
        return numeroPedidos;
    }

    public void setNumeroPedidos(Integer numeroPedidos) {
        this.numeroPedidos = numeroPedidos;
    }

    public BigDecimal getTicketMedio() {
        return ticketMedio;
    }

    public void setTicketMedio(BigDecimal ticketMedio) {
        this.ticketMedio = ticketMedio;
    }

    public BigDecimal getPercentualMudanca() {
        return percentualMudanca;
    }

    public void setPercentualMudanca(BigDecimal percentualMudanca) {
        this.percentualMudanca = percentualMudanca;
    }

    public String getSetinha() {
        return setinha;
    }

    public void setSetinha(String setinha) {
        this.setinha = setinha;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public BigDecimal getVendasOntem() {
        return vendasOntem;
    }

    public void setVendasOntem(BigDecimal vendasOntem) {
        this.vendasOntem = vendasOntem;
    }

    public Integer getPedidosOntem() {
        return pedidosOntem;
    }

    public void setPedidosOntem(Integer pedidosOntem) {
        this.pedidosOntem = pedidosOntem;
    }
}
