package com.bumh3r.repository;

import com.bumh3r.entity.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICarreraRepository extends JpaRepository<Carrera, Integer> {
    List<Carrera> findByActivo(Integer activo);
}