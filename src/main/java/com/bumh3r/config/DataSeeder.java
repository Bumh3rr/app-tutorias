package com.bumh3r.config;

import com.bumh3r.entity.*;
import com.bumh3r.repository.*;
import com.bumh3r.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private IUsuarioRepository usuarioRepository;
    @Autowired private ITutorRepository tutorRepository;
    @Autowired private ITutoradoRepository tutoradoRepository;
    @Autowired private ICoordinadorCarreraRepository coordinadorRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UsuarioService usuarioService;

    @Override
    public void run(String... args) {
        log.info("DataSeeder iniciado — verificando cuentas del sistema...");
        crearAdministrativos();
        sincronizarTutores();
        sincronizarTutorados();
        sincronizarCoordinadores();
        log.info("DataSeeder completado.");
    }

    private void crearAdministrativos() {
        crearSiNoExiste("dda@chilpancingo.tecnm.mx",     "dda2026",          Rol.DDA);
        crearSiNoExiste("cit@chilpancingo.tecnm.mx",     "cit2026",          Rol.CIT);
        crearSiNoExiste("subdirector@chilpancingo.tecnm.mx", "subdirector2026", Rol.SUBDIRECTOR);
    }

    private void crearSiNoExiste(String username, String password, Rol rol) {
        if (!usuarioRepository.existsByUsername(username)) {
            Usuario u = Usuario.builder()
                    .username(username)
                    .passwordHash(passwordEncoder.encode(password))
                    .rol(rol)
                    .activo(true)
                    .build();
            usuarioRepository.save(u);
            log.info("Cuenta creada: {} ({})", username, rol);
        }
    }

    private void sincronizarTutores() {
        List<Tutor> tutores = tutorRepository.findAll();
        for (Tutor t : tutores) {
            if (t.getUsuario() == null && t.getEmail() != null && !t.getEmail().isBlank()) {
                if (!usuarioRepository.existsByUsername(t.getEmail().toLowerCase())) {
                    try {
                        usuarioService.crearParaTutor(t);
                        log.info("Usuario creado para tutor id={} ({})", t.getId(), t.getEmail());
                    } catch (Exception e) {
                        log.warn("No se pudo crear usuario para tutor id={}: {}", t.getId(), e.getMessage());
                    }
                }
            }
        }
    }

    private void sincronizarTutorados() {
        List<Tutorado> tutorados = tutoradoRepository.findAll();
        for (Tutorado td : tutorados) {
            if (td.getUsuario() == null && td.getEmail() != null && !td.getEmail().isBlank()) {
                if (!usuarioRepository.existsByUsername(td.getEmail().toLowerCase())) {
                    try {
                        usuarioService.crearParaTutorado(td);
                        log.info("Usuario creado para tutorado id={} ({})", td.getId(), td.getEmail());
                    } catch (Exception e) {
                        log.warn("No se pudo crear usuario para tutorado id={}: {}", td.getId(), e.getMessage());
                    }
                }
            }
        }
    }

    private void sincronizarCoordinadores() {
        List<CoordinadorCarrera> coords = coordinadorRepository.findAll();
        for (CoordinadorCarrera c : coords) {
            if (c.getUsuario() == null && c.getEmail() != null && !c.getEmail().isBlank()) {
                if (!usuarioRepository.existsByUsername(c.getEmail().toLowerCase())) {
                    try {
                        usuarioService.crearParaCoordinador(c);
                        log.info("Usuario creado para coordinador id={} ({})", c.getId(), c.getEmail());
                    } catch (Exception e) {
                        log.warn("No se pudo crear usuario para coordinador id={}: {}", c.getId(), e.getMessage());
                    }
                }
            }
        }
    }
}
