package com.bumh3r.service;

import com.bumh3r.entity.GrupoTutorado;
import com.bumh3r.entity.Tutorado;
import java.util.List;

public interface GrupoTutoradoService {
    List<GrupoTutorado> obtenerTodosGrupoTutorados();
    void asignarTutorados(Integer idGrupo, Integer[] idsTutorados);
    void eliminarTutoradoDeGrupo(Integer id);
    List<GrupoTutorado> buscarPorGrupo(Integer idGrupo);
    List<GrupoTutorado> buscarTutoriasPorTutorado(Integer idTutorado);
    List<Tutorado> obtenerTutoradosDisponibles(Integer idGrupo);
}
