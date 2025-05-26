package com.eatease.eatease.service;

import com.eatease.eatease.dto.DashBoardDTO;
import com.eatease.eatease.model.Item;
import com.eatease.eatease.model.Pedido;
import com.eatease.eatease.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class DashBoardService {

    private final PedidoRepository pedidoRepository;
    private final ItemService itemService;

    public DashBoardService(PedidoRepository pedidoRepository, ItemService itemService) {
        this.pedidoRepository = pedidoRepository;
        this.itemService = itemService;
    }

    public DashBoardDTO calcularMetricasDashboard() {
        // Pegar data de hoje
        LocalDate hoje = LocalDate.now();
        LocalDate ontem = hoje.minusDays(1);

        // Calcular métricas de hoje
        BigDecimal vendasHoje = calcularVendasDia(hoje);
        int pedidosHoje = contarPedidosDia(hoje);
        BigDecimal ticketMedioHoje = calcularTicketMedio(vendasHoje, pedidosHoje);

        // Calcular métricas de ontem
        BigDecimal vendasOntem = calcularVendasDia(ontem);
        int pedidosOntem = contarPedidosDia(ontem);

        // Calcular comparação percentual com ontem
        BigDecimal percentualMudanca = calcularPercentualMudanca(vendasHoje, vendasOntem);
        String setinha = percentualMudanca.compareTo(BigDecimal.ZERO) >= 0 ? "↑" : "↓";
        String cor = percentualMudanca.compareTo(BigDecimal.ZERO) >= 0 ? "green" : "red";

        // Criar e popular DTO
        DashBoardDTO dashboard = new DashBoardDTO();
        dashboard.setVendasDia(vendasHoje);
        dashboard.setNumeroPedidos(pedidosHoje);
        dashboard.setTicketMedio(ticketMedioHoje);
        dashboard.setPercentualMudanca(percentualMudanca);
        dashboard.setSetinha(setinha);
        dashboard.setCor(cor);
        dashboard.setVendasOntem(vendasOntem);
        dashboard.setPedidosOntem(pedidosOntem);

        return dashboard;
    }

    public BigDecimal calcularVendasDia(LocalDate data) {
        List<Pedido> pedidos = buscarPedidosValidosDia(data);
        BigDecimal totalVendas = BigDecimal.ZERO;

        for (Pedido pedido : pedidos) {
            // Calcular valor total do pedido somando preços dos itens
            BigDecimal valorPedido = calcularValorPedido(pedido);
            totalVendas = totalVendas.add(valorPedido);
        }

        return totalVendas.setScale(2, RoundingMode.HALF_UP);
    }

    public int contarPedidosDia(LocalDate data) {
        List<Pedido> pedidos = buscarPedidosValidosDia(data);
        return pedidos.size();
    }

    public BigDecimal calcularTicketMedio(BigDecimal vendas, int numeroPedidos) {
        if (numeroPedidos == 0) {
            return BigDecimal.ZERO;
        }
        return vendas.divide(BigDecimal.valueOf(numeroPedidos), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularPercentualMudanca(BigDecimal vendasHoje, BigDecimal vendasOntem) {
        if (vendasOntem.compareTo(BigDecimal.ZERO) == 0) {
            // Se ontem não houve vendas, retorna 100% se hoje houve vendas, senão 0%
            return vendasHoje.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100.0") : BigDecimal.ZERO;
        }

        BigDecimal diferenca = vendasHoje.subtract(vendasOntem);
        BigDecimal percentual = diferenca.divide(vendasOntem, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return percentual.setScale(1, RoundingMode.HALF_UP);
    }

    private List<Pedido> buscarPedidosValidosDia(LocalDate data) {
        String dataInicio = data.atStartOfDay().toString();
        String dataFim = data.atTime(23, 59, 59).toString();

        return pedidoRepository.findValidPedidosByDateRange(dataInicio, dataFim);
    }

    private BigDecimal calcularValorPedido(Pedido pedido) {
        List<Long> itensIds = pedido.getItensIds();
        if (itensIds == null || itensIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<CompletableFuture<BigDecimal>> futures = itensIds.stream()
                .map(itemId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Item item = itemService.getByIdNoUpdate(itemId);
                        return item != null ? BigDecimal.valueOf(item.getPreco()) : BigDecimal.ZERO;
                    } catch (Exception e) {
                        System.err.println("Erro ao buscar item " + itemId + ": " + e.getMessage());
                        return BigDecimal.ZERO;
                    }
                }))
                .collect(Collectors.toList());

        // Espera que todos os futures acabem e soma os resultados
        return futures.stream()
                .map(CompletableFuture::join) // Espera que cada future termine
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String formatarComparacaoPercentual(BigDecimal percentual) {
        String sinal = percentual.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        String setinha = percentual.compareTo(BigDecimal.ZERO) >= 0 ? "↑" : "↓";
        return setinha + " " + sinal + percentual + "%";
    }

    /**
     * Obtém métricas detalhadas para um período específico
     * 
     * @param dataInicio data de início
     * @param dataFim    data de fim
     * @return DashBoardDTO com métricas do período
     */
    public DashBoardDTO calcularMetricasPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        BigDecimal vendasPeriodo = BigDecimal.ZERO;
        int pedidosPeriodo = 0;

        LocalDate dataAtual = dataInicio;
        while (!dataAtual.isAfter(dataFim)) {
            vendasPeriodo = vendasPeriodo.add(calcularVendasDia(dataAtual));
            pedidosPeriodo += contarPedidosDia(dataAtual);
            dataAtual = dataAtual.plusDays(1);
        }

        BigDecimal ticketMedio = calcularTicketMedio(vendasPeriodo, pedidosPeriodo);

        DashBoardDTO dashboard = new DashBoardDTO();
        dashboard.setVendasDia(vendasPeriodo);
        dashboard.setNumeroPedidos(pedidosPeriodo);
        dashboard.setTicketMedio(ticketMedio);
        dashboard.setPercentualMudanca(BigDecimal.ZERO); // Sem comparação para períodos
        dashboard.setSetinha("");
        dashboard.setCor("blue");

        return dashboard;
    }
}
