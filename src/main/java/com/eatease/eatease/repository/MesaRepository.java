package com.eatease.eatease.repository;

import com.eatease.eatease.model.Mesa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {
    Optional<Mesa> findByNumero(int numero);

    /**
     * Batch load mesas by IDs
     */
    @Query("SELECT m FROM Mesa m WHERE m.id IN :ids")
    List<Mesa> findByIdIn(@Param("ids") List<Long> ids);
}
