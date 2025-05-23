package com.eatease.eatease.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eatease.eatease.dto.MenuCreateDTO;
import com.eatease.eatease.model.Menu;
import com.eatease.eatease.service.*;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/menu")
@Validated
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createMenu(@Valid @RequestBody MenuCreateDTO menuCreateDTO,
            @Parameter(hidden = true) HttpServletRequest request) {
        // Verifica se o utilizador está autenticado
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        try {
            Menu res = menuService.createMenu(
                    menuCreateDTO.getNome(),
                    menuCreateDTO.getDescricao(),
                    menuCreateDTO.getItemsIds(),
                    menuCreateDTO.getTipoMenuId());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao cadastrar menu: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteMenu(@RequestParam Long id,
            @Parameter(hidden = true) HttpServletRequest request) {
        // Verifica se o utilizador está autenticado
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        if (menuService.deleteMenu(id)) {
            return ResponseEntity.ok("Menu eliminado com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("O menu não existe.");
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllMenus(@Parameter(hidden = true) HttpServletRequest request) {
        return ResponseEntity.ok(menuService.getAllMenus());
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateMenu(@RequestParam Long id,
            @Valid @RequestBody MenuCreateDTO menuCreateDTO,
            @Parameter(hidden = true) HttpServletRequest request) {
        // Verifica se o utilizador está autenticado
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        try {
            Menu res = menuService.updateMenu(
                    id,
                    menuCreateDTO.getNome(),
                    menuCreateDTO.getDescricao(),
                    menuCreateDTO.getItemsIds(),
                    menuCreateDTO.getTipoMenuId());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao editar menu: " + e.getMessage());
        }

    }

    @GetMapping("/getMenuItens")
    public ResponseEntity<?> getMenuItens(@RequestParam Long id,
            @Parameter(hidden = true) HttpServletRequest request) {
        try {
            return ResponseEntity.ok(menuService.getMenuItens(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao obter itens do menu: " + e.getMessage());
        }
    }
}
