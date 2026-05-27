package com.bumh3r.service;

import com.bumh3r.dto.PreviewGeneracionDTO;

import java.time.LocalDate;
import java.util.List;

public interface SesionGeneracionService {
    PreviewGeneracionDTO previewGeneracion(Integer idGrupo, LocalDate fechaInicio, List<Integer> idsPats);
    void generarSesiones(Integer idGrupo, LocalDate fechaInicio, List<Integer> idsPats);
}
