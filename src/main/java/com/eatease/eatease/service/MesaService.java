package com.eatease.eatease.service;

import com.eatease.eatease.model.Mesa;
import com.eatease.eatease.repository.MesaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;

    public MesaService(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    public Mesa createMesa(int numero, boolean estadoLivre, int capacidade) throws Exception {
        if (mesaRepository.findByNumero(numero).isEmpty()) {
            Mesa mesa = new Mesa();
            mesa.setNumero(numero);
            mesa.setEstadoLivre(estadoLivre);
            mesa.setPos_x(0); // Posição X padrão
            mesa.setPos_y(0); // Posição Y padrão
            mesa.setCapacidade(capacidade);
            mesaRepository.save(mesa);
            System.err.println("Mesa adicionada com sucesso.");
            return mesa;
        } else {
            System.err.println("A mesa já existe.");
            throw new IllegalArgumentException("A mesa já existe.");
        }
    }

    public void SetMesaPos(long id, float pos_x, float pos_y) {
        Optional<Mesa> optMesa = mesaRepository.findById(id);
        if (optMesa.isPresent()) {
            Mesa mesa = optMesa.get();
            mesa.setPos_x(pos_x);
            mesa.setPos_y(pos_y);
            mesaRepository.save(mesa);
            System.err.println("Posição da mesa " + mesa.getNumero() + " atualizada.");
        } else {
            System.err.println("A mesa não existe.");
        }
    }

    public float getX(long id) {
        return mesaRepository.findById(id)
                .map(Mesa::getPos_x)
                .orElseThrow(() -> new IllegalArgumentException("Mesa não encontrada com ID: " + id));
    }

    public float getY(long id) {
        return mesaRepository.findById(id)
                .map(Mesa::getPos_y)
                .orElseThrow(() -> new IllegalArgumentException("Mesa não encontrada com ID: " + id));
    }

    public List<Mesa> getAllMesas() {
        return mesaRepository.findAll();
    }

    public Optional<Mesa> getMesaById(long id) {
        return mesaRepository.findById(id);
    }

    public Optional<Mesa> getMesaByNumero(int numero) {
        return mesaRepository.findByNumero(numero);
    }

    public boolean deleteMesa(long id) {
        if (mesaRepository.existsById(id)) {
            mesaRepository.deleteById(id);
            System.err.println("Mesa removida com sucesso.");
            return true;
        } else {
            System.err.println("A mesa não existe.");
            return false;
        }
    }

    public boolean setMesaOcupada(long id) {
        Optional<Mesa> optMesa = mesaRepository.findById(id);
        if (optMesa.isPresent()) {
            Mesa mesa = optMesa.get();
            mesa.setEstadoLivre(false);
            mesaRepository.save(mesa);
            System.err.println("Mesa " + mesa.getNumero() + " definida como ocupada.");
            return true;
        } else {
            System.err.println("A mesa não existe.");
            return false;
        }
    }

    public boolean setMesaOcupadaByNumero(int numero) {
        Optional<Mesa> optMesa = mesaRepository.findByNumero(numero);
        if (optMesa.isPresent()) {
            Mesa mesa = optMesa.get();
            mesa.setEstadoLivre(false);
            mesaRepository.save(mesa);
            System.err.println("Mesa " + numero + " definida como ocupada.");
            return true;
        } else {
            System.err.println("A mesa não existe.");
            return false;
        }
    }

    public boolean setMesaLivre(long id) {
        Optional<Mesa> optMesa = mesaRepository.findById(id);
        if (optMesa.isPresent()) {
            Mesa mesa = optMesa.get();
            mesa.setEstadoLivre(true);
            mesaRepository.save(mesa);
            System.err.println("Mesa " + mesa.getNumero() + " definida como livre.");
            return true;
        } else {
            System.err.println("A mesa não existe.");
            return false;
        }
    }

    public boolean setMesaLivreByNumero(int numero) {
        Optional<Mesa> optMesa = mesaRepository.findByNumero(numero);
        if (optMesa.isPresent()) {
            Mesa mesa = optMesa.get();
            mesa.setEstadoLivre(true);
            mesaRepository.save(mesa);
            System.err.println("Mesa " + numero + " definida como livre.");
            return true;
        } else {
            System.err.println("A mesa não existe.");
            return false;
        }
    }

    /**
     * Batch load mesas by IDs
     * 
     * @param ids List of mesa IDs to load
     * @return List of mesas
     */
    public List<Mesa> getMesasByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return mesaRepository.findByIdIn(ids);
    }
}
