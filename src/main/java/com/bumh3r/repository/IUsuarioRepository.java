package com.bumh3r.repository;

import com.bumh3r.entity.Rol;
import com.bumh3r.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
    List<Usuario> findByRol(Rol rol);
    List<Usuario> findByActivoAndRol(Boolean activo, Rol rol);
    List<Usuario> findByActivo(Boolean activo);
}
