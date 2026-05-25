package com.bumh3r.service;

import com.bumh3r.dto.CoordinadorNotificacionDTO;
import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.CoordinadorCarrera;

import java.util.List;
import java.util.Map;

public interface NotificacionService {

    // ── Envío ─────────────────────────────────────────────────────────────
    Map<String, Integer> enviarRecordatoriosSemanales();
    boolean enviarRecordatorioCoordinador(Integer idCoordinador);

    // ── Actividades ───────────────────────────────────────────────────────
    List<Actividad> obtenerActividadesSemanaActual();

    // ── Preview ───────────────────────────────────────────────────────────
    String renderPreviewHtml(Integer idCoordinador);

    // ── Filtrado y resumen ────────────────────────────────────────────────
    List<Actividad> filtrarActividadesPorCoordinador(CoordinadorCarrera coord, List<Actividad> todasActividades);
    List<CoordinadorCarrera> obtenerCoordinadoresVigentes();
    List<CoordinadorNotificacionDTO> obtenerResumenCoordinadores();

    /** Retorna las actividades aplicables a un coordinador específico.
     *  Null si el coordinador no existe, está inactivo o no está en el semestre vigente. */
    List<Actividad> obtenerActividadesAplicablesParaCoordinador(Integer idCoordinador);
}
