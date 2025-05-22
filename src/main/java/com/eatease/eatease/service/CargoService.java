package com.eatease.eatease.service;

import com.eatease.eatease.model.Cargo;
import com.eatease.eatease.repository.CargoRepository;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class CargoService {

    private final CargoRepository cargoRepository;

    public CargoService(CargoRepository cargoRepository) {
        this.cargoRepository = cargoRepository;
    }

    public boolean createCargo(String nome) {
        if (cargoRepository.findByNome(nome).isEmpty()) {
            Cargo cargo = new Cargo();
            cargo.setNome(nome);
            cargoRepository.save(cargo);
            System.err.println("Cargo adicionado com sucesso.");
            return true;
        } else {
            System.err.println("O cargo já existe.");
            return false;
        }
    }

    public Optional<Cargo> findById(Long id) {
        return cargoRepository.findById(id);
    }

    public boolean checkCargoIdExists(Long id) {
        return cargoRepository.existsById(id);
    }

    public long getCargoId(String nome) {
        return cargoRepository.findByNome(nome)
                .map(Cargo::getId)
                .orElse(-1L);
    }

}
