package com.eatease.eatease.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eatease.eatease.model.Funcionario;
import com.eatease.eatease.model.Mesa;
import com.eatease.eatease.service.FuncionarioService;
import com.eatease.eatease.service.Login;
import com.eatease.eatease.service.MesaService;
import com.eatease.eatease.service.QRService;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;

@RestController
@RequestMapping("/qr")
@Validated
public class QRController {
    private final QRService qrService;
    private final FuncionarioService funcionarioService;
    private final MesaService mesaService;

    public QRController(QRService qrService, FuncionarioService funcionarioService, MesaService mesaService) {
        this.mesaService = mesaService;
        this.qrService = qrService;
        this.funcionarioService = funcionarioService;
    }

    @GetMapping("/generateQR")
    public ResponseEntity<?> createPedidoQR(
            @RequestParam Long mesaID,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - GERENTE, COZINHEIRO e FUNCIONARIO podem criar
        // pedidos
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE", "COZINHEIRO", "FUNCIONARIO");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }

        // get from validUsername
        Funcionario funcionario = funcionarioService.findByUsername(validUsername).orElse(null);
        if (funcionario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Funcionário não encontrado");
        }

        Optional<Mesa> mesaOpt = mesaService.getMesaById(mesaID);
        if (mesaOpt.isEmpty()) {
            System.err.println("A mesa não existe.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mesa não encontrada");
        }

        try {
            byte[] qr = qrService.generateQRCodeImage(qrService.generateKey(funcionario.getId(), mesaID), 25, 25);
            mesaService.setMesaOcupada(mesaID);
            return ResponseEntity.ok(qr);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar QR Code: " + e.getMessage());
        }
    }

    @GetMapping("/qrPage")
    public ResponseEntity<String> getQRPage(@RequestParam String key) {
        if (!qrService.isKeyValidAndUnused(key)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("QR Code inválido ou já utilizado");
        }
        try {
            Resource resource = new ClassPathResource("templates/qrindex.html");
            String content = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()),
                    StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reading HTML file: " + e.getMessage());
        }
    }
}
