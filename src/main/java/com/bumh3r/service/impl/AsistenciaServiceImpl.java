package com.bumh3r.service.impl;

import com.bumh3r.dto.ResumenAsistenciaDTO;
import com.bumh3r.entity.Asistencia;
import com.bumh3r.entity.Sesion;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.repository.IAsistenciaRepository;
import com.bumh3r.repository.ISesionRepository;
import com.bumh3r.repository.ITutoradoRepository;
import com.bumh3r.service.AsistenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Service
public class AsistenciaServiceImpl implements AsistenciaService {

    private static final int TOTAL_SESIONES = 10;
    private static final double PORCENTAJE_MINIMO = 80.0;

    @Autowired
    private IAsistenciaRepository iAsistenciaRepository;
    @Autowired
    private ISesionRepository iSesionRepository;
    @Autowired
    private ITutoradoRepository iTutoradoRepository;

    @Override
    public List<Asistencia> obtenerTodasAsistencias() {
        return this.iAsistenciaRepository.findByActivo(1);
    }

    @Override
    public void guardarAsistencia(Asistencia asistencia) {
        resolverRelaciones(asistencia);
        asistencia.setActivo(1);
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
        asistenciaDB.setActivo(asistencia.getActivo());

        this.iAsistenciaRepository.save(asistenciaDB);
    }

    @Override
    public Asistencia obtenerAsistencia(Integer id) {
        return this.iAsistenciaRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarAsistencia(Integer id) {
        Asistencia asistencia = this.iAsistenciaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Asistencia no encontrada"));
        asistencia.setActivo(0);
        this.iAsistenciaRepository.save(asistencia);
    }

    @Override
    public List<Asistencia> buscarAsistenciasPorSesion(Integer idSesion) {
        Sesion sesion = this.iSesionRepository.findById(idSesion)
                .orElseThrow(() -> new NoSuchElementException("Sesión no encontrada"));
        return this.iAsistenciaRepository.findByActivoAndSesion(1, sesion);
    }

    @Override
    public List<Asistencia> buscarAsistenciasPorTutorado(Integer idTutorado) {
        Tutorado tutorado = this.iTutoradoRepository.findById(idTutorado)
                .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));
        return this.iAsistenciaRepository.findByActivoAndTutorado(1, tutorado);
    }

    @Override
    public void registrarAsistenciaMasiva(Integer idSesion, Integer[] idsTutoradosPresentes) {
        Sesion sesion = this.iSesionRepository.findById(idSesion)
                .orElseThrow(() -> new NoSuchElementException("Sesión no encontrada"));

        if ("CANCELADA".equals(sesion.getEstatusRegistro())) {
            throw new IllegalStateException(
                    "No se puede registrar asistencia en una sesión cancelada.");
        }

        // Obtener todos los tutorados asignados al tutor de la sesión
        List<Tutorado> todosTutorados = this.iTutoradoRepository.findByActivo(1);

        for (Tutorado tutorado : todosTutorados) {
            // Evitar duplicados
            if (this.iAsistenciaRepository.existsBySesionAndTutoradoAndActivo(sesion, tutorado, 1)) {
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
                    .activo(1)
                    .build();

            this.iAsistenciaRepository.save(asistencia);
        }

        // Marcar sesión como REALIZADA
        sesion.setEstatusRegistro("REALIZADA");
        this.iSesionRepository.save(sesion);
    }

    @Override
    public ResumenAsistenciaDTO calcularResumenAsistencia(Integer idTutorado) {
        Tutorado tutorado = this.iTutoradoRepository.findById(idTutorado)
                .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));

        long presentes = this.iAsistenciaRepository
                .countByTutoradoAndPresenteAndActivo(tutorado, 1, 1);

        long recuperadas = this.iAsistenciaRepository
                .countByTutoradoAndRecuperadaAndActivo(tutorado, 1, 1);

        long totalAcreditadas = presentes + recuperadas;
        double porcentaje = ((double) totalAcreditadas / TOTAL_SESIONES) * 100;
        boolean acreditado = porcentaje >= PORCENTAJE_MINIMO;

        return ResumenAsistenciaDTO.builder()
                .idTutorado(tutorado.getId())
                .nombreTutorado(tutorado.getNombre() + " " + tutorado.getApellido())
                .totalSesiones(TOTAL_SESIONES)
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
}