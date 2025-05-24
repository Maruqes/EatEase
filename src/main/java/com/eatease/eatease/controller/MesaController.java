package com.eatease.eatease.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.eatease.eatease.model.Mesa;
import com.eatease.eatease.service.MesaService;
import com.eatease.eatease.service.Login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * Controller para gerenciar mesas do restaurante
 */
@RestController
@RequestMapping("/mesa")
@Validated
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    /**
     * Cria uma nova mesa (apenas GERENTE pode criar)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createMesa(
            @RequestParam @NotNull @Min(1) int numero,
            @RequestParam(defaultValue = "true") boolean estadoLivre,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - apenas GERENTE pode criar mesas
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }

        try {
            Mesa result = mesaService.createMesa(numero, estadoLivre);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao cadastrar mesa: " + e.getMessage());
        }
    }

    /**
     * Busca todas as mesas do restaurante
     */
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllMesas(@Parameter(hidden = true) HttpServletRequest request) {
        // Verificação de autenticação - todos os funcionários podem ver as mesas
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        List<Mesa> mesas = mesaService.getAllMesas();
        return ResponseEntity.ok(mesas);
    }

    /**
     * Busca uma mesa pelo seu ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMesaById(
            @PathVariable long id,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - todos os funcionários podem ver as mesas
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        Optional<Mesa> mesa = mesaService.getMesaById(id);
        if (mesa.isPresent()) {
            return ResponseEntity.ok(mesa.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mesa não encontrada");
        }
    }

    /**
     * Busca uma mesa pelo seu número
     */
    @GetMapping("/numero/{numero}")
    public ResponseEntity<?> getMesaByNumero(
            @PathVariable int numero,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - todos os funcionários podem ver as mesas
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        Optional<Mesa> mesa = mesaService.getMesaByNumero(numero);
        if (mesa.isPresent()) {
            return ResponseEntity.ok(mesa.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mesa não encontrada");
        }
    }

    /**
     * Remove uma mesa pelo seu ID (apenas GERENTE pode remover)
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMesa(
            @RequestParam long id,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - apenas GERENTE pode remover mesas
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }

        boolean result = mesaService.deleteMesa(id);
        if (result) {
            return ResponseEntity.ok("Mesa removida com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mesa não encontrada");
        }
    }

    /**
     * Define uma mesa como ocupada
     */
    @PostMapping("/ocupar")
    public ResponseEntity<String> setMesaOcupada(
            @RequestParam long id,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - GERENTE, COZINHEIRO e FUNCIONARIO podem alterar
        // estado
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        boolean result = mesaService.setMesaOcupada(id);
        if (result) {
            return ResponseEntity.ok("Mesa definida como ocupada com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mesa não encontrada");
        }
    }

    /**
     * Define uma mesa como ocupada pelo número
     */
    @PostMapping("/ocupar/numero")
    public ResponseEntity<String> setMesaOcupadaByNumero(
            @RequestParam int numero,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - GERENTE, COZINHEIRO e FUNCIONARIO podem alterar
        // estado
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        boolean result = mesaService.setMesaOcupadaByNumero(numero);
        if (result) {
            return ResponseEntity.ok("Mesa definida como ocupada com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mesa não encontrada");
        }
    }

    /**
     * Define uma mesa como livre
     */
    @PostMapping("/liberar")
    public ResponseEntity<String> setMesaLivre(
            @RequestParam long id,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - GERENTE, COZINHEIRO e FUNCIONARIO podem alterar
        // estado
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        boolean result = mesaService.setMesaLivre(id);
        if (result) {
            return ResponseEntity.ok("Mesa definida como livre com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mesa não encontrada");
        }
    }

    /**
     * update server
     * Define uma mesa como livre pelo número
     */
    @PostMapping("/liberar/numero")
    public ResponseEntity<String> setMesaLivreByNumero(
            @RequestParam int numero,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - GERENTE, COZINHEIRO e FUNCIONARIO podem alterar
        // estado
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        boolean result = mesaService.setMesaLivreByNumero(numero);
        if (result) {
            return ResponseEntity.ok("Mesa definida como livre com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mesa não encontrada");
        }
    }

    @GetMapping("/getMesaNumberById")
    public ResponseEntity<String> getMesaNumberById(
            @RequestParam long mesaId,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - todos os funcionários podem ver as mesas
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        Optional<Mesa> mesaOpt = mesaService.getMesaById(mesaId);
        if (mesaOpt.isPresent()) {
            return ResponseEntity.ok(String.valueOf(mesaOpt.get().getNumero()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mesa não encontrada");
        }
    }
}
