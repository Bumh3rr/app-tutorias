package com.bumh3r.service.impl;

import com.bumh3r.dto.ResumenAsistenciaDTO;
import com.bumh3r.entity.Asistencia;
import com.bumh3r.entity.Grupo;
import com.bumh3r.entity.GrupoTutorado;
import com.bumh3r.entity.Sesion;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.repository.IAsistenciaRepository;
import com.bumh3r.repository.IGrupoTutoradoRepository;
import com.bumh3r.repository.ISesionRepository;
import com.bumh3r.repository.ITutoradoRepository;
import com.bumh3r.service.AsistenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Primary
@Service
public class AsistenciaServiceImpl implements AsistenciaService {

    private static final double PORCENTAJE_MINIMO = 80.0;

    @Autowired
    private IAsistenciaRepository iAsistenciaRepository;
    @Autowired
    private ISesionRepository iSesionRepository;
    @Autowired
    private ITutoradoRepository iTutoradoRepository;
    @Autowired
    private IGrupoTutoradoRepository iGrupoTutoradoRepository;

    @Override
    public List<Asistencia> obtenerTodasAsistencias() {
        return this.iAsistenciaRepository.findAll();
    }

    @Override
    public Page<Asistencia> obtenerTodasAsistenciasPage(Pageable pageable) {
        return this.iAsistenciaRepository.findAll(pageable);
    }

    @Override
    public void guardarAsistencia(Asistencia asistencia) {
        resolverRelaciones(asistencia);
        this.iAsistenciaRepository.save(asistencia);
    }

    @Override
    public void actualizarAsistencia(Integer id, Asistencia asistencia) {
        Asistencia asistenciaDB = this.iAsistenciaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Asistencia no encontrada"));

        resolverRelaciones(asistencia);

        asistenciaDB.setSesion(asistencia.getSesion());
        asistenciaDB.setTutorado(asistencia.getTutorado());
        asistenciaDB.setPresente(asistencia.getPresente());
        asistenciaDB.setRecuperada(asistencia.getRecuperada());

        this.iAsistenciaRepository.save(asistenciaDB);
    }

    @Override
    public Asistencia obtenerAsistencia(Integer id) {
        return this.iAsistenciaRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarAsistencia(Integer id) {
        if (!this.iAsistenciaRepository.existsById(id)) {
            throw new NoSuchElementException("Asistencia no encontrada");
        }
        this.iAsistenciaRepository.deleteById(id);
    }

    @Override
    public List<Asistencia> buscarAsistenciasPorSesion(Integer idSesion) {
        Sesion sesion = this.iSesionRepository.findById(idSesion)
                .orElseThrow(() -> new NoSuchElementException("Sesión no encontrada"));
        return this.iAsistenciaRepository.findBySesion(sesion);
    }

    @Override
    public List<Asistencia> buscarAsistenciasPorTutorado(Integer idTutorado) {
        Tutorado tutorado = this.iTutoradoRepository.findById(idTutorado)
                .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));
        return this.iAsistenciaRepository.findByTutorado(tutorado);
    }

    @Override
    public void registrarAsistenciaMasiva(Integer idSesion, Integer[] idsTutoradosPresentes) {
        Sesion sesion = this.iSesionRepository.findById(idSesion)
                .orElseThrow(() -> new NoSuchElementException("Sesión no encontrada"));

        if ("CANCELADA".equals(sesion.getEstatusRegistro())) {
            throw new IllegalStateException(
                    "No se puede registrar asistencia en una sesión cancelada.");
        }

        List<GrupoTutorado> grupoTutorados = this.iGrupoTutoradoRepository
                .findByActivoAndGrupo(1, sesion.getGrupo());
        List<Tutorado> todosTutorados = grupoTutorados.stream()
                .map(GrupoTutorado::getTutorado)
                .collect(Collectors.toList());

        for (Tutorado tutorado : todosTutorados) {
            if (this.iAsistenciaRepository.existsBySesionAndTutorado(sesion, tutorado)) {
                continue;
            }

            boolean estaPresente = false;
            if (idsTutoradosPresentes != null) {
                for (Integer idPresente : idsTutoradosPresentes) {
                    if (idPresente.equals(tutorado.getId())) {
                        estaPresente = true;
                        break;
                    }
                }
            }

            Asistencia asistencia = Asistencia.builder()
                    .sesion(sesion)
                    .tutorado(tutorado)
                    .presente(estaPresente ? 1 : 0)
                    .recuperada(0)
                    .build();

            this.iAsistenciaRepository.save(asistencia);
        }

        sesion.setEstatusRegistro("REALIZADA");
        this.iSesionRepository.save(sesion);
    }

    @Override
    public ResumenAsistenciaDTO calcularResumenAsistencia(Integer idTutorado) {
        Tutorado tutorado = this.iTutoradoRepository.findById(idTutorado)
                .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));

        long presentes = this.iAsistenciaRepository.countByTutoradoAndPresente(tutorado, 1);
        long recuperadas = this.iAsistenciaRepository.countByTutoradoAndRecuperada(tutorado, 1);

        List<GrupoTutorado> gruposTutorado = this.iGrupoTutoradoRepository.findByActivoAndTutorado(1, tutorado);
        List<Grupo> grupos = gruposTutorado.stream()
                .map(GrupoTutorado::getGrupo)
                .collect(Collectors.toList());

        long totalSesiones;
        if (!grupos.isEmpty()) {
            totalSesiones = this.iSesionRepository.countByGruposAndEstatusRegistroIn(
                    grupos, List.of("REALIZADA", "PENDIENTE"));
        } else {
            totalSesiones = 0;
        }
        if (totalSesiones == 0) {
            totalSesiones = 10;
        }

        long totalAcreditadas = presentes + recuperadas;
        double porcentaje = ((double) totalAcreditadas / totalSesiones) * 100;
        boolean acreditado = porcentaje >= PORCENTAJE_MINIMO;

        return ResumenAsistenciaDTO.builder()
                .idTutorado(tutorado.getId())
                .nombreTutorado(tutorado.getNombre() + " " + tutorado.getApellido())
                .totalSesiones(totalSesiones)
                .asistenciasPresente(presentes)
                .asistenciasRecuperadas(recuperadas)
                .totalAcreditadas(totalAcreditadas)
                .porcentaje(Math.round(porcentaje * 10.0) / 10.0)
                .acreditado(acreditado)
                .build();
    }

    private void resolverRelaciones(Asistencia asistencia) {
        if (asistencia.getSesion() != null && asistencia.getSesion().getId() != null) {
            Sesion sesion = this.iSesionRepository.findById(asistencia.getSesion().getId())
                    .orElseThrow(() -> new NoSuchElementException("Sesión no encontrada"));
            asistencia.setSesion(sesion);
        } else {
            asistencia.setSesion(null);
        }

        if (asistencia.getTutorado() != null && asistencia.getTutorado().getId() != null) {
            Tutorado tutorado = this.iTutoradoRepository.findById(asistencia.getTutorado().getId())
                    .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));
            asistencia.setTutorado(tutorado);
        } else {
            asistencia.setTutorado(null);
        }
    }

    @Override
    public java.util.List<com.bumh3r.entity.Asistencia> buscarAsistenciasPorFechaRegistro(java.util.Date inicio, java.util.Date fin) {
        return this.iAsistenciaRepository.findByFechaRegistroBetween(inicio, fin);
    }
}
