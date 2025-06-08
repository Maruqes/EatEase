package com.eatease.eatease.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.eatease.eatease.dto.IngredientesRequestDTO;
import com.eatease.eatease.dto.IngredientesResponseDTO;
import com.eatease.eatease.model.Ingredientes;
import com.eatease.eatease.service.IngredientesService;
import com.eatease.eatease.service.Login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;

import io.swagger.v3.oas.annotations.Parameter; // springdoc-openapi

@RestController
@RequestMapping("/ingredientes")
@Validated
public class IngredientesController {
    private final IngredientesService ingredientesService;

    public IngredientesController(IngredientesService ingredientesService) {
        this.ingredientesService = ingredientesService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createIngrediente(
            @Valid @RequestBody IngredientesRequestDTO requestDTO,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verifica se o utilizador está autenticado
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        try {
            Ingredientes res = ingredientesService.createIngredientes(
                    requestDTO.getNome(),
                    requestDTO.getStock(),
                    requestDTO.getStock_min(),
                    requestDTO.getUnidadeMedida());
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao cadastrar ingrediente: " + e.getMessage());
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllIngredientes(@Parameter(hidden = true) HttpServletRequest request) {
        List<Ingredientes> ingredientes = ingredientesService.getAllIngredientes();
        return ResponseEntity.ok(ingredientes);
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editIngrediente(
            @RequestParam long id,
            @Valid @RequestBody IngredientesRequestDTO requestDTO,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verifica se o utilizador está autenticado
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        try {
            Ingredientes res = ingredientesService.updateIngredientes(
                    id,
                    requestDTO.getNome(),
                    requestDTO.getStock(),
                    requestDTO.getStock_min(),
                    requestDTO.getUnidadeMedida());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao editar ingrediente: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteIngrediente(
            @RequestParam long id,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verifica se o utilizador está autenticado
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        try {
            boolean res = ingredientesService.deleteIngredientes(id);
            if (res) {
                return ResponseEntity.ok("Ingrediente removido com sucesso.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("O ingrediente não existe.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao remover ingrediente: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIngredienteById(
            @PathVariable long id,
            @Parameter(hidden = true) HttpServletRequest request) {
        Ingredientes ingrediente = ingredientesService.getIngredienteById(id);
        if (ingrediente != null) {
            return ResponseEntity.ok(IngredientesResponseDTO.fromEntity(ingrediente));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Ingrediente não encontrado.");
        }
    }
}
