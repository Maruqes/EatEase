package com.eatease.eatease.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.eatease.eatease.dto.IngredienteQuantDTO;
import com.eatease.eatease.dto.ItemRequestDTO;
import com.eatease.eatease.model.Item;
import com.eatease.eatease.service.ItemService;
import com.eatease.eatease.service.Login;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter; // springdoc-openapi

import java.util.List;
import java.util.Optional;

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
        Item item = itemService.getByIdUpdate(pratoId);
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

    @PostMapping("/setFoto")
    public ResponseEntity<?> setFoto(
            @RequestParam long itemId,
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verifica se o utilizador está autenticado
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ficheiro não fornecido");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Apenas ficheiros de imagem são permitidos");
        }

        // Check file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ficheiro muito grande. Máximo 5MB");
        }

        try {
            String filename = itemService.updateItemPhoto(itemId, file);
            return ResponseEntity.ok("Foto atualizada com sucesso. Nome do ficheiro: " + filename);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao guardar foto: " + e.getMessage());
        }
    }

    @GetMapping("/getFoto/{itemId}")
    public ResponseEntity<?> getFoto(@PathVariable long itemId) {
        try {
            Optional<Item> itemOpt = itemService.getItemById(itemId);
            if (itemOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item não encontrado");
            }

            Item item = itemOpt.get();
            if (item.getFoto() == null || item.getFoto().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item não tem foto");
            }

            // Return the photo filename/path
            return ResponseEntity.ok().body("{\"foto\": \"" + item.getFoto() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao obter foto: " + e.getMessage());
        }
    }

    @DeleteMapping("/deleteFoto/{itemId}")
    public ResponseEntity<?> deleteFoto(
            @PathVariable long itemId,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verifica se o utilizador está autenticado
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        try {
            boolean result = itemService.deleteItemPhoto(itemId);
            if (result) {
                return ResponseEntity.ok("Foto eliminada com sucesso");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item não encontrado ou não tem foto");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao eliminar foto: " + e.getMessage());
        }
    }
}
