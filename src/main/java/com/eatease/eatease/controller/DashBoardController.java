package com.eatease.eatease.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.eatease.eatease.dto.DashBoardDTO;
import com.eatease.eatease.dto.ItemProfitDTO;
import com.eatease.eatease.model.Item;
import com.eatease.eatease.service.DashBoardService;
import com.eatease.eatease.service.Login;

import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Controller para gerenciar dashboard e métricas financeiras
 */
@RestController
@RequestMapping("/dashboard")
@Validated
public class DashBoardController {

    private final DashBoardService dashBoardService;

    public DashBoardController(DashBoardService dashBoardService) {
        this.dashBoardService = dashBoardService;
    }

    /**
     * Obtém métricas do dashboard para hoje
     * Inclui: vendas do dia, número de pedidos, ticket médio e comparação com ontem
     */
    @GetMapping("/metrics")
    public ResponseEntity<?> getDashboardMetrics(@Parameter(hidden = true) HttpServletRequest request) {
        // Verificação de autenticação - apenas GERENTE pode ver métricas
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }

        try {
            DashBoardDTO metrics = dashBoardService.calcularMetricasDashboard();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao calcular métricas: " + e.getMessage());
        }
    }

    /**
     * Obtém vendas de um dia específico
     */
    @GetMapping("/vendas-dia")
    public ResponseEntity<?> getVendasDia(
            @RequestParam String data,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - apenas GERENTE pode ver métricas
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }

        try {
            LocalDate dataConsulta = LocalDate.parse(data, DateTimeFormatter.ISO_LOCAL_DATE);
            var vendas = dashBoardService.calcularVendasDia(dataConsulta);
            return ResponseEntity.ok(String.format("€%.2f", vendas));
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Formato de data inválido. Use: YYYY-MM-DD");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao calcular vendas: " + e.getMessage());
        }
    }

    /**
     * Obtém número de pedidos de um dia específico
     */
    @GetMapping("/pedidos-dia")
    public ResponseEntity<?> getPedidosDia(
            @RequestParam String data,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - apenas GERENTE pode ver métricas
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }

        try {
            LocalDate dataConsulta = LocalDate.parse(data, DateTimeFormatter.ISO_LOCAL_DATE);
            int numeroPedidos = dashBoardService.contarPedidosDia(dataConsulta);
            return ResponseEntity.ok(numeroPedidos);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Formato de data inválido. Use: YYYY-MM-DD");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao contar pedidos: " + e.getMessage());
        }
    }

    /**
     * Obtém métricas para um período específico
     */
    @GetMapping("/periodo")
    public ResponseEntity<?> getMetricasPeriodo(
            @RequestParam String dataInicio,
            @RequestParam String dataFim,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Verificação de autenticação - apenas GERENTE pode ver métricas
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado ou sem permissões");
        }

        try {
            LocalDate inicio = LocalDate.parse(dataInicio, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate fim = LocalDate.parse(dataFim, DateTimeFormatter.ISO_LOCAL_DATE);

            if (inicio.isAfter(fim)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Data de início deve ser anterior à data de fim");
            }

            DashBoardDTO metrics = dashBoardService.calcularMetricasPeriodo(inicio, fim);
            return ResponseEntity.ok(metrics);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Formato de data inválido. Use: YYYY-MM-DD");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao calcular métricas do período: " + e.getMessage());
        }
    }

    @GetMapping("/bestItem")
    public ResponseEntity<?> getBestItem(
            @RequestParam int lastDays,
            @RequestParam(required = false, defaultValue = "0") int position,
            @Parameter(hidden = true) HttpServletRequest request) {
        // Verificação de autenticação - apenas GERENTE pode ver métricas
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        try {
            Item bestItem = dashBoardService.getBestItem(lastDays, position);
            if (bestItem != null) {
                return ResponseEntity.ok(bestItem);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/lucroByItemId")
    public ResponseEntity<?> getLucroByItemId(
            @RequestParam long itemId,
            @RequestParam int lastDays,
            @Parameter(hidden = true) HttpServletRequest request) {
        // Verificação de autenticação - apenas GERENTE pode ver métricas
        String validUsername = Login.checkLoginWithCargos(request, "GERENTE");
        if (validUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        try {
            ItemProfitDTO lucroInfo = dashBoardService.calcularLucroPorItem(itemId, lastDays);
            return ResponseEntity.ok(lucroInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao calcular lucro: " + e.getMessage());
        }
    }
}
