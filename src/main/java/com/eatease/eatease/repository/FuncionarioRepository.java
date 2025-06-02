package com.eatease.eatease.repository;

import com.eatease.eatease.model.Funcionario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByUsername(String username);

    /**
     * Batch load funcionarios by IDs
     */
    @Query("SELECT f FROM Funcionario f WHERE f.id IN :ids")
    List<Funcionario> findByIdIn(@Param("ids") List<Long> ids);
}
