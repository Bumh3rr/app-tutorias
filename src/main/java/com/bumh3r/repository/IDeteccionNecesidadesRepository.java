package com.bumh3r.repository;

import com.bumh3r.entity.DeteccionNecesidades;
import com.bumh3r.entity.Sesion;
import com.bumh3r.entity.Tutorado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDeteccionNecesidadesRepository extends JpaRepository<DeteccionNecesidades, Integer> {

    List<DeteccionNecesidades> findByActivo(Integer activo);

    Page<DeteccionNecesidades> findByActivo(Integer activo, Pageable pageable);

    // Por tutorado
    List<DeteccionNecesidades> findByActivoAndTutorado(Integer activo, Tutorado tutorado);

    // Por sesión
    List<DeteccionNecesidades> findByActivoAndSesion(Integer activo, Sesion sesion);

    // Alumnos con necesidad académica en álgebra
    List<DeteccionNecesidades> findByActivoAndNecesidadAlgebra(Integer activo, Integer necesidadAlgebra);

    // Alumnos con necesidad académica en cálculo
    List<DeteccionNecesidades> findByActivoAndNecesidadCalculo(Integer activo, Integer necesidadCalculo);

    // Alumnos con necesidad económica
    List<DeteccionNecesidades> findByActivoAndNecesidadEconomica(Integer activo, Integer necesidadEconomica);

    // Alumnos con necesidad psicológica
    List<DeteccionNecesidades> findByActivoAndNecesidadPsicologica(Integer activo, Integer necesidadPsicologica);

    // Verificar si ya existe detección para un tutorado en una sesión
    boolean existsByTutoradoAndSesionAndActivo(Tutorado tutorado, Sesion sesion, Integer activo);
}