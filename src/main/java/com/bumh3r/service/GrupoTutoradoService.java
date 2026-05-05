package com.bumh3r.service;

import com.bumh3r.entity.GrupoTutorado;
import com.bumh3r.entity.Tutorado;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;

public interface GrupoTutoradoService {
    List<GrupoTutorado> obtenerTodosGrupoTutorados();
    void asignarTutorados(Integer idGrupo, Integer[] idsTutorados);
    void eliminarTutoradoDeGrupo(Integer id);
    List<GrupoTutorado> buscarPorGrupo(Integer idGrupo);
    List<GrupoTutorado> buscarTutoriasPorTutorado(Integer idTutorado);
    List<Tutorado> obtenerTutoradosDisponibles(Integer idGrupo);
    Map<Integer, Long> contarAlumnosPorGrupo();
    Page<GrupoTutorado> buscarHistorial(String q, Integer idSemestre, Integer idCarrera, int page, int pageSize);
}
