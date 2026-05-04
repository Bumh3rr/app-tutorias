package com.bumh3r.service;

import com.bumh3r.entity.Grupo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GrupoService {
    List<Grupo> obtenerTodosGrupos();
    void guardarGrupo(Grupo grupo);
    void actualizarGrupo(Integer id, Grupo grupo);
    Grupo obtenerGrupo(Integer id);
    void eliminarGrupo(Integer id);

    List<Grupo> buscarPorSemestre(Integer idSemestre);
    List<Grupo> buscarPorTutor(Integer idTutor);
    List<Grupo> buscarPorTutorYSemestre(Integer idTutor, Integer idSemestre);
    List<Grupo> buscarPorCarreraYSemestre(Integer idCarrera, Integer idSemestre);

    Page<Grupo> obtenerTodosGruposPage(Pageable pageable);
    Page<Grupo> buscarPorSemestrePage(Integer idSemestre, Pageable pageable);
    Page<Grupo> buscarPorTutorYSemestrePage(Integer idTutor, Integer idSemestre, Pageable pageable);
    Page<Grupo> buscarPorCarreraYSemestrePage(Integer idCarrera, Integer idSemestre, Pageable pageable);
}
