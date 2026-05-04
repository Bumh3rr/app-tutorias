package com.bumh3r.service;

import com.bumh3r.entity.Asistencia;
import com.bumh3r.dto.ResumenAsistenciaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AsistenciaService {
    List<Asistencia> obtenerTodasAsistencias();
    Page<Asistencia> obtenerTodasAsistenciasPage(Pageable pageable);
    void guardarAsistencia(Asistencia asistencia);
    void actualizarAsistencia(Integer id, Asistencia asistencia);
    Asistencia obtenerAsistencia(Integer id);
    void eliminarAsistencia(Integer id);

    // Asistencias de una sesión
    List<Asistencia> buscarAsistenciasPorSesion(Integer idSesion);

    // Historial de un tutorado
    List<Asistencia> buscarAsistenciasPorTutorado(Integer idTutorado);

    // Registrar asistencia masiva de una sesión
    void registrarAsistenciaMasiva(Integer idSesion, Integer[] idsTutoradosPresentes);

    // Calcular porcentaje de asistencia de un tutorado
    ResumenAsistenciaDTO calcularResumenAsistencia(Integer idTutorado);
}