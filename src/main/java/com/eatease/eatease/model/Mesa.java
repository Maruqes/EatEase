package com.eatease.eatease.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mesa")

public class Mesa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int numero;
    private boolean estadoLivre; // true=livre, false=ocupado

    private float pos_x;
    private float pos_y;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public boolean isEstadoLivre() {
        return estadoLivre;
    }

    public void setEstadoLivre(boolean estadoLivre) {
        this.estadoLivre = estadoLivre;
    }

    public float getPos_x() {
        return pos_x;
    }

    public void setPos_x(float pos_x) {
        this.pos_x = pos_x;
    }

    public float getPos_y() {
        return pos_y;
    }

    public void setPos_y(float pos_y) {
        this.pos_y = pos_y;
    }

}
