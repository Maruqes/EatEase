package com.eatease.eatease.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.eatease.eatease.dto.ItemRequestDTO;
import com.eatease.eatease.model.Item;
import com.eatease.eatease.service.ItemService;
import com.eatease.eatease.service.Login;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter; // springdoc-openapi

@RestController
@RequestMapping("/item")
@Validated
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createItem(
            @Valid @RequestBody ItemRequestDTO requestDTO,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verifica se o utilizador está autenticado
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        try {
            Item res = itemService.createItem(
                    requestDTO.getNome(),
                    requestDTO.getTipoPratoId(),
                    requestDTO.getPreco(),
                    requestDTO.getIngredientes(),
                    requestDTO.isComposto(),
                    0);
            itemService.SetCalculatedStockByItemId(res.getId());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao cadastrar item: " + e.getMessage());
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllItems(@Parameter(hidden = true) HttpServletRequest request) {

        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping("/getByPratoId")
    public ResponseEntity<?> getByPratoId(@RequestParam long pratoId,
            @Parameter(hidden = true) HttpServletRequest request) {
        Item item = itemService.getById(pratoId);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item não encontrado.");
        }
        return ResponseEntity.ok(item);
    }

    @PostMapping("/edit")
    public ResponseEntity<?> editItem(
            @RequestParam long id,
            @Valid @RequestBody ItemRequestDTO requestDTO,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verifica se o utilizador está autenticado
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        try {
            Item res = itemService.editItem(
                    id,
                    requestDTO.getNome(),
                    requestDTO.getTipoPratoId(),
                    requestDTO.getPreco(),
                    requestDTO.getIngredientes(),
                    requestDTO.isComposto(),
                    0);
            itemService.SetCalculatedStockByItemId(res.getId());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao editar item: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteItem(
            @RequestParam long id,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verifica se o utilizador está autenticado
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        try {
            boolean res = itemService.deleteItem(id);
            if (res) {
                return ResponseEntity.ok("Item eliminado com sucesso.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Não foi possível eliminar o item.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
