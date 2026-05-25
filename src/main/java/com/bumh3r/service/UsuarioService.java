package com.bumh3r.service;

import com.bumh3r.entity.CoordinadorCarrera;
import com.bumh3r.entity.Rol;
import com.bumh3r.entity.Tutor;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.entity.Usuario;

import java.util.Optional;

public interface UsuarioService {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
    Usuario crearUsuario(String username, String passwordRaw, Rol rol);
    Usuario crearParaTutor(Tutor tutor);
    Usuario crearParaTutorado(Tutorado tutorado);
    Usuario crearParaCoordinador(CoordinadorCarrera coord);
    void actualizarUsername(Integer idUsuario, String nuevoUsername);
    void cambiarPassword(Integer idUsuario, String nuevaPasswordRaw);
    void desactivar(Integer idUsuario);
}
