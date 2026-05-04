package com.bumh3r.repository;

import com.bumh3r.entity.Carrera;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICarreraRepository extends JpaRepository<Carrera, Integer> {
    List<Carrera> findByActivo(Integer activo);
    Page<Carrera> findByActivo(Integer activo, Pageable pageable);

    boolean existsByClaveAndActivo(String clave, Integer activo);
    boolean existsByClaveAndActivoAndIdNot(String clave, Integer activo, Integer id);

    @Query("SELECT c FROM Carrera c WHERE (:q IS NULL OR :q = '' OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Carrera> searchByName(@Param("q") String q, Pageable pageable);
}