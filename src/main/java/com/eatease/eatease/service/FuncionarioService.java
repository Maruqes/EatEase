package com.eatease.eatease.service;

import com.eatease.eatease.model.Funcionario;
import com.eatease.eatease.repository.FuncionarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final CargoService cargoService;

    public FuncionarioService(FuncionarioRepository funcionarioRepository, CargoService cargoService) {
        this.funcionarioRepository = funcionarioRepository;
        this.cargoService = cargoService;
    }

    public Funcionario createFuncionario(String nome, long cargoId, String username, String password, String email,
            String telefone) throws Exception {
        if (cargoService.checkCargoIdExists(cargoId) == false) {
            System.err.println("O cargo não existe.");
            throw new IllegalArgumentException("O cargo não existe.");
        }

        if (funcionarioRepository.findByUsername(username).isPresent()) {
            System.err.println("O funcionário já existe.");
            throw new IllegalArgumentException("O funcionário já existe.");
        }

        if (funcionarioRepository.findByUsername(username).isEmpty()) {
            Funcionario funcionario = new Funcionario();
            funcionario.setNome(nome);
            funcionario.setCargoId(cargoId); // Define a associação com o Cargo
            funcionario.setUsername(username);
            funcionario.setPassword(Login.hashPassword(password));
            funcionario.setEmail(email);
            funcionario.setTelefone(telefone);
            funcionarioRepository.save(funcionario);
            System.err.println("Funcionário adicionado com sucesso.");
            return funcionario;
        } else {
            System.err.println("O funcionário " + username + " já existe.");
            return null;
        }
    }

    public Optional<Funcionario> findByUsername(String username) {
        return funcionarioRepository.findByUsername(username);
    }

    /**
     * Verifica se o funcionário possui um determinado cargo pelo ID do cargo
     * 
     * @param funcionarioId ID do funcionário
     * @param cargoId       ID do cargo a ser verificado
     * @return true se o funcionário possui o cargo especificado, false caso
     *         contrário
     */
    public boolean checkCargoByID(long funcionarioId, long cargoId) {
        Optional<Funcionario> funcionario = funcionarioRepository.findById(funcionarioId);
        return funcionario.isPresent() && funcionario.get().getCargoId() == cargoId;
    }

    /**
     * Verifica se o funcionário possui um determinado cargo pelo nome do cargo
     * 
     * @param funcionarioId ID do funcionário
     * @param cargoNome     Nome do cargo a ser verificado
     * @return true se o funcionário possui o cargo especificado, false caso
     *         contrário
     */
    public boolean checkCargoByIDAndName(long funcionarioId, String cargoNome) {
        Optional<Funcionario> funcionario = funcionarioRepository.findById(funcionarioId);
        if (funcionario.isEmpty()) {
            return false;
        }

        long funcionarioCargoId = funcionario.get().getCargoId();
        return cargoService.findById(funcionarioCargoId)
                .map(cargo -> cargo.getNome().equalsIgnoreCase(cargoNome))
                .orElse(false);
    }

    /**
     * Verifica se o funcionário possui um determinado cargo pelo username do
     * funcionário
     * 
     * @param username Username do funcionário
     * @param cargoId  ID do cargo a ser verificado
     * @return true se o funcionário possui o cargo especificado, false caso
     *         contrário
     */
    public boolean checkCargoByUsername(String username, long cargoId) {
        Optional<Funcionario> funcionario = findByUsername(username);
        return funcionario.isPresent() && funcionario.get().getCargoId() == cargoId;
    }

    /**
     * Verifica se o funcionário possui um determinado cargo pelo username do
     * funcionário e nome do cargo
     * 
     * @param username  Username do funcionário
     * @param cargoNome Nome do cargo a ser verificado
     * @return true se o funcionário possui o cargo especificado, false caso
     *         contrário
     */
    public boolean checkCargoByUsernameAndName(String username, String cargoNome) {
        Optional<Funcionario> funcionario = findByUsername(username);
        if (funcionario.isEmpty()) {
            return false;
        }

        long funcionarioCargoId = funcionario.get().getCargoId();
        return cargoService.findById(funcionarioCargoId)
                .map(cargo -> cargo.getNome().equalsIgnoreCase(cargoNome))
                .orElse(false);
    }

    public boolean deleteFuncionario(long funcionarioId) {
        Optional<Funcionario> funcionario = funcionarioRepository.findById(funcionarioId);
        if (funcionario.isPresent()) {
            funcionarioRepository.delete(funcionario.get());
            return true;
        } else {
            return false;
        }
    }

    public List<Funcionario> getAllFuncionarios() {
        return funcionarioRepository.findAll();
    }

    public Funcionario updateFuncionario(long funcionarioId, String nome, long cargoId, String username,
            String password,
            String email, String telefone) throws Exception {
        Optional<Funcionario> funcionario = funcionarioRepository.findById(funcionarioId);

        if (funcionario.isEmpty()) {
            System.err.println("O funcionário não existe.");
            throw new IllegalArgumentException("O funcionário não existe.");
        }

        if (cargoService.checkCargoIdExists(cargoId) == false) {
            System.err.println("O cargo não existe.");
            throw new IllegalArgumentException("O cargo não existe.");
        }

        if (!funcionario.get().getUsername().equals(username)) {
            System.out.println("O username deve ser sempre o mesmo.");
            throw new IllegalArgumentException("O username deve ser sempre o mesmo.");
        }

        if (funcionario.isPresent()) {
            Funcionario f = funcionario.get();
            f.setNome(nome);
            f.setCargoId(cargoId);
            f.setUsername(username);
            f.setPassword(Login.hashPassword(password));
            f.setEmail(email);
            f.setTelefone(telefone);
            funcionarioRepository.save(f);
            return f;
        } else {
            throw new IllegalArgumentException("O funcionário não existe.");
        }
    }

    public Optional<Funcionario> getFuncionarioById(long id) {
        return funcionarioRepository.findById(id);
    }
}
