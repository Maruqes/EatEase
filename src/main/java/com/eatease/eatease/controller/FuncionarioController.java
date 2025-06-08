// src/main/java/com/eatease/eatease/controller/FuncionarioController.java
package com.eatease.eatease.controller;

import org.springframework.http.ResponseEntity;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Parameter; // springdoc-openapi

import com.eatease.eatease.dto.FuncionarioDTO;
import com.eatease.eatease.dto.LoginRequestDTO;
import com.eatease.eatease.model.Funcionario;
import com.eatease.eatease.service.CargoService;
import com.eatease.eatease.service.FuncionarioService;
import com.eatease.eatease.service.Login;

@RestController
@RequestMapping("/auth")
@Validated
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService,
            CargoService cargoService) {
        this.funcionarioService = funcionarioService;
        Login.setFuncionarioService(funcionarioService);
        Login.setCargoService(cargoService);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody FuncionarioDTO dto,
            @Parameter(hidden = true) HttpServletRequest request) {

        String validUsername = Login.checkLoginWithCargos(request, "GERENTE");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }

        try {
            Funcionario res = funcionarioService.createFuncionario(dto.getNome(), dto.getCargoId(), dto.getUsername(),
                    dto.getPassword(), dto.getEmail(), dto.getTelefone());
            res.setPassword(null);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao cadastrar funcionário: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody LoginRequestDTO loginRequest,
            @Parameter(hidden = true) // esconder do Swagger
            HttpServletResponse response) {

        String validUsername = Login.login(loginRequest.getUsername(), loginRequest.getPassword(), response);
        return validUsername == null
                ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas")
                : ResponseEntity.ok("Login bem-sucedido");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response) {

        String validUsername = Login.checkLogin(request);
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }

        Login.logout(request, response);
        return ResponseEntity.ok("Logout bem-sucedido");
    }

    @DeleteMapping("/deleteFuncionario")
    public ResponseEntity<String> deleteFuncionario(@RequestParam long funcionarioId,
            @Parameter(hidden = true) HttpServletRequest request) {

        String validUsername = Login.checkLoginWithCargos(request, "GERENTE");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }

        boolean res = funcionarioService.deleteFuncionario(funcionarioId);
        if (res) {
            return ResponseEntity.ok("Funcionário removido com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Não foi possível remover o funcionário.");
        }
    }

    @GetMapping("/getAllFuncionarios")
    public ResponseEntity<?> getAllFuncionarios(@Parameter(hidden = true) HttpServletRequest request) {

        String validUsername = Login.checkLoginWithCargos(request, "GERENTE");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }

        return ResponseEntity.ok(funcionarioService.getAllFuncionarios());
    }

    @GetMapping("/getFuncionarioNameById")
    public ResponseEntity<String> getFuncionarioNameById(@RequestParam long funcionarioId,
            @Parameter(hidden = true) HttpServletRequest request) {

        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }
        Optional<Funcionario> funcionario = funcionarioService.getFuncionarioById(funcionarioId);
        if (funcionario != null) {
            return ResponseEntity.ok(funcionario.map(Funcionario::getNome)
                    .orElse("Funcionário não encontrado"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Funcionário não encontrado");
        }
    }

    @PutMapping("/updateFuncionario")
    public ResponseEntity<?> updateFuncionario(@RequestParam long funcionarioId,
            @RequestBody FuncionarioDTO dto,
            @Parameter(hidden = true) HttpServletRequest request) {

        String validUsername = Login.checkLoginWithCargos(request, "GERENTE");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }

        try {
            Funcionario res = funcionarioService.updateFuncionario(funcionarioId, dto.getNome(), dto.getCargoId(),
                    dto.getUsername(), dto.getPassword(), dto.getEmail(), dto.getTelefone());
            res.setPassword(null);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao atualizar funcionário: " + e.getMessage());
        }
    }
}
