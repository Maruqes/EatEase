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

@Service
public class DashBoardService {

    private final PedidoRepository pedidoRepository;
    private final ItemService itemService;

    public DashBoardService(PedidoRepository pedidoRepository, ItemService itemService) {
        this.pedidoRepository = pedidoRepository;
        this.itemService = itemService;
    }

    /**
     * Calcula todas as métricas do dashboard para hoje
     * 
     * @return DashBoardDTO com todas as métricas
     */
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

    /**
     * Calcula o total de vendas (faturamento) de um dia específico
     * 
     * @param data data para calcular
     * @return valor total faturado em euros
     */
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

    /**
     * Conta o número de pedidos de um dia específico
     * 
     * @param data data para contar
     * @return número de pedidos
     */
    public int contarPedidosDia(LocalDate data) {
        List<Pedido> pedidos = buscarPedidosValidosDia(data);
        return pedidos.size();
    }

    /**
     * Calcula o ticket médio (valor médio por pedido)
     * 
     * @param vendas        total de vendas
     * @param numeroPedidos número de pedidos
     * @return ticket médio
     */
    public BigDecimal calcularTicketMedio(BigDecimal vendas, int numeroPedidos) {
        if (numeroPedidos == 0) {
            return BigDecimal.ZERO;
        }
        return vendas.divide(BigDecimal.valueOf(numeroPedidos), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula a percentual de mudança entre hoje e ontem
     * 
     * @param vendasHoje  vendas de hoje
     * @param vendasOntem vendas de ontem
     * @return percentual de mudança
     */
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

    /**
     * Busca pedidos válidos (não cancelados) de um dia específico
     * 
     * @param data data para buscar
     * @return lista de pedidos válidos
     */
    private List<Pedido> buscarPedidosValidosDia(LocalDate data) {
        String dataInicio = data.atStartOfDay().toString();
        String dataFim = data.atTime(23, 59, 59).toString();

        return pedidoRepository.findValidPedidosByDateRange(dataInicio, dataFim);
    }

    /**
     * Calcula o valor total de um pedido específico
     * 
     * @param pedido pedido para calcular
     * @return valor total do pedido
     */
    private BigDecimal calcularValorPedido(Pedido pedido) {
        BigDecimal valorTotal = BigDecimal.ZERO;

        List<Long> itensIds = pedido.getItensIds();
        if (itensIds != null) {
            for (Long itemId : itensIds) {
                try {
                    Item item = itemService.getByIdNoUpdate(itemId);
                    if (item != null) {
                        valorTotal = valorTotal.add(BigDecimal.valueOf(item.getPreco()));
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao buscar item " + itemId + ": " + e.getMessage());
                }
            }
        }

        return valorTotal;
    }

    /**
     * Método auxiliar para formatar o percentual com sinal e seta
     * 
     * @param percentual valor percentual
     * @return string formatada (ex: "↑ +8.5%" ou "↓ -3.2%")
     */
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
