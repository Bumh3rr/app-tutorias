package com.bumh3r.service;

import com.bumh3r.entity.EvidenciaSesion;
import java.util.List;

public interface EvidenciaSesionService {
    List<EvidenciaSesion> obtenerTodasEvidencias();
    void guardarEvidencia(EvidenciaSesion evidencia);
    void actualizarEvidencia(Integer id, EvidenciaSesion evidencia);
    EvidenciaSesion obtenerEvidencia(Integer id);
    void eliminarEvidencia(Integer id);

    List<EvidenciaSesion> buscarEvidenciasPorSesion(Integer idSesion);
    void validarEvidencia(Integer id, String notas);
    void rechazarEvidencia(Integer id, String notas);
}