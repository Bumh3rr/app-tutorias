package com.bumh3r.service.impl;

import com.bumh3r.entity.CoordinadorCarrera;
import com.bumh3r.entity.Rol;
import com.bumh3r.entity.Tutor;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.entity.Usuario;
import com.bumh3r.repository.IUsuarioRepository;
import com.bumh3r.service.SessionRegistryService;
import com.bumh3r.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired private IUsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private SessionRegistryService sessionRegistryService;

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public Usuario crearUsuario(String username, String passwordRaw, Rol rol) {
        Usuario u = Usuario.builder()
                .username(username.toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(passwordRaw))
                .rol(rol)
                .activo(true)
                .build();
        return usuarioRepository.save(u);
    }

    @Override
    @Transactional
    public Usuario crearParaTutor(Tutor tutor) {
        String username = tutor.getEmail() != null ? tutor.getEmail().toLowerCase().trim()
                : generarEmailFallback(tutor.getNombre(), tutor.getApellido());
        String password = tutor.getNumeroControl() != null ? tutor.getNumeroControl() : generarPasswordRandom();
        Usuario u = Usuario.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .rol(Rol.TUTOR)
                .activo(true)
                .tutor(tutor)
                .build();
        return usuarioRepository.save(u);
    }

    @Override
    @Transactional
    public Usuario crearParaTutorado(Tutorado tutorado) {
        String username = tutorado.getEmail() != null ? tutorado.getEmail().toLowerCase().trim()
                : generarEmailFallback(tutorado.getNombre(), tutorado.getApellido());
        String password = tutorado.getNumeroControl() != null ? tutorado.getNumeroControl() : generarPasswordRandom();
        if (tutorado.getNumeroControl() == null) {
            log.warn("Tutorado id={} sin número de control — se generó password aleatorio: {}",
                    tutorado.getId(), password);
        }
        Usuario u = Usuario.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .rol(Rol.TUTORADO)
                .activo(true)
                .tutorado(tutorado)
                .build();
        return usuarioRepository.save(u);
    }

    @Override
    @Transactional
    public Usuario crearParaCoordinador(CoordinadorCarrera coord) {
        String username = coord.getEmail() != null ? coord.getEmail().toLowerCase().trim()
                : generarEmailFallback(coord.getNombre(), coord.getApellido());
        String password = coord.getNumeroControl() != null ? coord.getNumeroControl() : generarPasswordRandom();
        Usuario u = Usuario.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .rol(Rol.COORDINADOR)
                .activo(true)
                .coordinador(coord)
                .build();
        return usuarioRepository.save(u);
    }

    @Override
    @Transactional
    public void actualizarUsername(Integer idUsuario, String nuevoUsername) {
        usuarioRepository.findById(idUsuario).ifPresent(u -> {
            u.setUsername(nuevoUsername.toLowerCase().trim());
            usuarioRepository.save(u);
        });
    }

    @Override
    @Transactional
    public void cambiarPassword(Integer idUsuario, String nuevaPasswordRaw) {
        usuarioRepository.findById(idUsuario).ifPresent(u -> {
            u.setPasswordHash(passwordEncoder.encode(nuevaPasswordRaw));
            usuarioRepository.save(u);
        });
    }

    @Override
    @Transactional
    public void desactivar(Integer idUsuario) {
        usuarioRepository.findById(idUsuario).ifPresent(u -> {
            u.setActivo(false);
            usuarioRepository.save(u);
        });
    }

    @Override
    @Transactional
    public void desactivarPorTutor(Integer tutorId) {
        usuarioRepository.findByTutorId(tutorId).ifPresent(u -> {
            u.setActivo(false);
            usuarioRepository.save(u);
            sessionRegistryService.invalidarSesionesDeUsuario(u.getUsername());
        });
    }

    @Override
    @Transactional
    public void reactivarPorTutor(Integer tutorId) {
        usuarioRepository.findByTutorId(tutorId).ifPresent(u -> {
            u.setActivo(true);
            usuarioRepository.save(u);
        });
    }

    @Override
    @Transactional
    public void desactivarPorTutorado(Integer tutoradoId) {
        usuarioRepository.findByTutoradoId(tutoradoId).ifPresent(u -> {
            u.setActivo(false);
            usuarioRepository.save(u);
            sessionRegistryService.invalidarSesionesDeUsuario(u.getUsername());
        });
    }

    @Override
    @Transactional
    public void reactivarPorTutorado(Integer tutoradoId) {
        usuarioRepository.findByTutoradoId(tutoradoId).ifPresent(u -> {
            u.setActivo(true);
            usuarioRepository.save(u);
        });
    }

    @Override
    @Transactional
    public void desactivarPorCoordinador(Integer coordinadorId) {
        usuarioRepository.findByCoordinadorId(coordinadorId).ifPresent(u -> {
            u.setActivo(false);
            usuarioRepository.save(u);
            sessionRegistryService.invalidarSesionesDeUsuario(u.getUsername());
        });
    }

    @Override
    @Transactional
    public void reactivarPorCoordinador(Integer coordinadorId) {
        usuarioRepository.findByCoordinadorId(coordinadorId).ifPresent(u -> {
            u.setActivo(true);
            usuarioRepository.save(u);
        });
    }

    private String generarEmailFallback(String nombre, String apellido) {
        String n = normalizar(nombre.split(" ")[0]);
        String a = normalizar(apellido.split(" ")[0]);
        return n + "." + a + "@tecnm.mx";
    }

    private String normalizar(String s) {
        return s.toLowerCase()
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ü", "u")
                .replace("ñ", "n").replaceAll("[^a-z0-9]", "");
    }

    private String generarPasswordRandom() {
        return Long.toHexString(Double.doubleToLongBits(Math.random())).substring(0, 8);
    }
}
