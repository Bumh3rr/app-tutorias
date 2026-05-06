package com.bumh3r.service;

import com.bumh3r.entity.EvidenciaSesion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface EvidenciaSesionService {
    List<EvidenciaSesion> obtenerTodasEvidencias();
    Page<EvidenciaSesion> obtenerTodasEvidenciasPage(Pageable pageable);
    void guardarEvidencia(EvidenciaSesion evidencia);
    void actualizarEvidencia(Integer id, EvidenciaSesion evidencia);
    EvidenciaSesion obtenerEvidencia(Integer id);
    void eliminarEvidencia(Integer id);

    List<EvidenciaSesion> buscarEvidenciasPorSesion(Integer idSesion);
    void validarEvidencia(Integer id, String notas);
    void rechazarEvidencia(Integer id, String notas);

    List<EvidenciaSesion> buscarEvidenciasPorFechaRegistro(java.util.Date inicio, java.util.Date fin);
}
