package com.bumh3r.repository;

import com.bumh3r.entity.Semestre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISemestreRepository extends JpaRepository<Semestre, Integer> {
    List<Semestre> findByActivo(Integer activo);
}