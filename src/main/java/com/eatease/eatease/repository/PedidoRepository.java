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

    /**
     * Fetch all pedidos with optimized data loading for fast retrieval
     * Uses native query to reduce database round trips
     */
    @Query(value = """
            SELECT
                p.id as pedido_id,
                p.itensIds as itens_ids,
                p.estadoPedido_id,
                p.mesa_id,
                p.funcionario_id,
                p.dataHora,
                p.observacao,
                p.ingredientesRemover,
                m.numero as mesa_numero,
                f.nome as funcionario_nome
            FROM pedido p
            LEFT JOIN mesa m ON p.mesa_id = m.id
            LEFT JOIN funcionario f ON p.funcionario_id = f.id
            """, nativeQuery = true)
    List<Object[]> findAllPedidosWithJoinedData();
}
