package com.bumh3r.repository;

import com.bumh3r.entity.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ITutorRepository extends JpaRepository<Tutor, Integer> {

    List<Tutor> findByActivo(Integer activo);

    Page<Tutor> findByActivo(Integer activo, Pageable pageable);

    boolean existsByNumeroControlAndActivo(String numeroControl, Integer activo);
    boolean existsByNumeroControlAndActivoAndIdNot(String numeroControl, Integer activo, Integer id);

    boolean existsByEmailAndActivo(String email, Integer activo);
    boolean existsByEmailAndActivoAndIdNot(String email, Integer activo, Integer id);

    @Query("SELECT t FROM Tutor t WHERE t.activo = 1 AND (:q IS NULL OR :q = '' OR LOWER(CONCAT(t.nombre, ' ', t.apellido)) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Tutor> searchByName(@Param("q") String q, Pageable pageable);
}
