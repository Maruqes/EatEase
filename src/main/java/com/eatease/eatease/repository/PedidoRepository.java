package com.eatease.eatease.repository;

import com.eatease.eatease.model.Pedido;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Busca pedidos criados em uma data específica
     * 
     * @param dataInicio início do dia (ex: "2024-01-01T00:00:00")
     * @param dataFim    fim do dia (ex: "2024-01-01T23:59:59")
     * @return lista de pedidos do dia
     */
    @Query("SELECT p FROM Pedido p WHERE p.dataHora >= :dataInicio AND p.dataHora <= :dataFim")
    List<Pedido> findPedidosByDateRange(@Param("dataInicio") String dataInicio, @Param("dataFim") String dataFim);

    /**
     * Busca pedidos de hoje (estado diferente de cancelado - ID 4)
     * 
     * @param dataInicio início de hoje
     * @param dataFim    fim de hoje
     * @return lista de pedidos válidos de hoje
     */
    @Query("SELECT p FROM Pedido p WHERE p.dataHora >= :dataInicio AND p.dataHora <= :dataFim AND p.estadoPedido_id != 4")
    List<Pedido> findValidPedidosByDateRange(@Param("dataInicio") String dataInicio, @Param("dataFim") String dataFim);
}
