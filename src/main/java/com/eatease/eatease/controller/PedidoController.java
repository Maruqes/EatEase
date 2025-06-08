package com.eatease.eatease.controller;

import com.eatease.eatease.model.Pedido;
import com.eatease.eatease.dto.PedidoFastGetDTO;
import com.eatease.eatease.dto.PedidoRequestDTO;
import com.eatease.eatease.service.PedidoService;
import com.eatease.eatease.service.QRService;
import com.eatease.eatease.service.QRService.QRData;
import com.eatease.eatease.service.Login;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

/**
 * Controller para gerenciar pedidos do restaurante
 */
@RestController
@RequestMapping("/pedido")
@Validated
public class PedidoController {

    private final PedidoService pedidoService;
    private final QRService qrService;

    public PedidoController(PedidoService pedidoService, QRService qrService) {
        this.pedidoService = pedidoService;
        this.qrService = qrService;
    }

    /**
     * Cria um novo pedido
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPedido(
            @Valid @RequestBody PedidoRequestDTO pedidoDTO,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - GERENTE, COZINHEIRO e FUNCIONARIO podem criar
        // pedidos
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }

        try {
            Pedido result = pedidoService.createPedido(
                    pedidoDTO.getItensIds(),
                    pedidoDTO.getMesaId(),
                    pedidoDTO.getFuncionarioId(),
                    pedidoDTO.getObservacao(), pedidoDTO.getIngredientesRemover());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao cadastrar pedido: " + e.getMessage());
        }
    }

    @PostMapping("/pedirQR")
    public ResponseEntity<?> createPedidoQR(
            @RequestParam String key,
            @Valid @RequestBody PedidoRequestDTO pedidoDTO,
            @Parameter(hidden = true) HttpServletRequest request) {

        if (qrService.isKeyValidAndUnused(key) == false) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao cadastrar pedido: QR Code inválido");
        }

        try {
            QRData qrData = qrService.getQRData(key);
            Pedido result = pedidoService.createPedido(
                    pedidoDTO.getItensIds(),
                    qrData.getMesaId(),
                    qrData.getFuncionarioId(),
                    pedidoDTO.getObservacao(), pedidoDTO.getIngredientesRemover());
            // Marca o QR Code como usado
            qrService.markAsUsed(key);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao cadastrar pedido: " + e.getMessage());
        }
    }

    /**
     * Busca todos os pedidos
     */
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllPedidos(@Parameter(hidden = true) HttpServletRequest request) {
        // Verificação de autenticação - todos os funcionários podem ver os pedidos
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        List<Pedido> pedidos = pedidoService.getAllPedidos();
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Busca todos os pedidos
     */
    @GetMapping("/getAllRapid")
    public ResponseEntity<?> getAllPedidosUtraFastModernPhosmorphicFast2(
            @Parameter(hidden = true) HttpServletRequest request) {
        // Verificação de autenticação - todos os funcionários podem ver os pedidos
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        List<PedidoFastGetDTO> pedidos = pedidoService.getAllPedidosFastGet();
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Busca um pedido pelo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPedidoById(
            @PathVariable long id,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - todos os funcionários podem ver os pedidos
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado");
        }

        Optional<Pedido> pedido = pedidoService.getPedidoById(id);
        if (pedido.isPresent()) {
            return ResponseEntity.ok(pedido.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pedido não encontrado");
        }
    }

    /**
     * Remove um pedido
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deletePedido(
            @RequestParam long id,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - apenas GERENTE pode remover pedidos
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }

        boolean result = pedidoService.deletePedido(id);
        if (result) {
            return ResponseEntity.ok("Pedido removido com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pedido não encontrado");
        }
    }

    @PutMapping("/setEstado")
    public ResponseEntity<String> setEstado(
            @RequestParam long id,
            @RequestParam long estadoPedido_id,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - GERENTE, COZINHEIRO e FUNCIONARIO podem editar
        // pedidos
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }

        String result = pedidoService.setEstadoPedido(id, estadoPedido_id);
        if (result == null) {
            return ResponseEntity.ok("Estado do pedido atualizado com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }
}
