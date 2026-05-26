package com.bumh3r.service.impl;

import com.bumh3r.entity.Grupo;
import com.bumh3r.entity.GrupoTutorado;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.repository.IGrupoRepository;
import com.bumh3r.repository.IGrupoTutoradoRepository;
import com.bumh3r.repository.ITutoradoRepository;
import com.bumh3r.service.GrupoTutoradoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Primary
@Service
public class GrupoTutoradoServiceImpl implements GrupoTutoradoService {

    @Autowired
    private IGrupoTutoradoRepository iGrupoTutoradoRepository;
    @Autowired
    private IGrupoRepository iGrupoRepository;
    @Autowired
    private ITutoradoRepository iTutoradoRepository;

    @Override
    public List<GrupoTutorado> obtenerTodosGrupoTutorados() {
        return this.iGrupoTutoradoRepository.findByActivo(1);
    }

    @Override
    public List<String> asignarTutorados(Integer idGrupo, Integer[] idsTutorados) {
        Grupo grupo = this.iGrupoRepository.findById(idGrupo)
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado"));

        List<String> advertencias = new java.util.ArrayList<>();

        if (idsTutorados == null || idsTutorados.length == 0) {
            return advertencias;
        }

        Semestre semestreDestino = grupo.getSemestre();

        for (Integer idTutorado : idsTutorados) {
            Tutorado tutorado = this.iTutoradoRepository.findById(idTutorado)
                    .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado: " + idTutorado));

            if (this.iGrupoTutoradoRepository.existsByGrupoAndTutoradoAndActivo(grupo, tutorado, 1)) {
                continue;
            }

            // Validación defensiva: un tutorado no puede estar en dos grupos del mismo semestre
            if (semestreDestino != null) {
                List<GrupoTutorado> enMismoSemestre = this.iGrupoTutoradoRepository
                        .findByTutoradoAndGrupoSemestreAndActivo(tutorado, semestreDestino, 1);
                if (!enMismoSemestre.isEmpty()) {
                    advertencias.add("El tutorado " + tutorado.getNombre() + " " + tutorado.getApellido()
                            + " ya está asignado a otro grupo en este semestre.");
                    continue;
                }
            }

            long totalGrupos = this.iGrupoTutoradoRepository.countByTutoradoAndActivo(tutorado, 1);
            if (totalGrupos >= 2) {
                advertencias.add("El tutorado " + tutorado.getNombre() + " " + tutorado.getApellido()
                        + " ya tiene el máximo de 2 asignaciones activas permitidas.");
                continue;
            }

            GrupoTutorado gt = GrupoTutorado.builder()
                    .grupo(grupo)
                    .tutorado(tutorado)
                    .activo(1)
                    .build();
            this.iGrupoTutoradoRepository.save(gt);
        }

        return advertencias;
    }

    @Override
    public void eliminarTutoradoDeGrupo(Integer id) {
        GrupoTutorado gt = this.iGrupoTutoradoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Asignación no encontrada"));
        gt.setActivo(0);
        this.iGrupoTutoradoRepository.save(gt);
    }

    @Override
    public List<GrupoTutorado> buscarPorGrupo(Integer idGrupo) {
        Grupo grupo = this.iGrupoRepository.findById(idGrupo)
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado"));
        return this.iGrupoTutoradoRepository.findActiveByGrupo(grupo);
    }

    @Override
    public List<GrupoTutorado> buscarTutoriasPorTutorado(Integer idTutorado) {
        Tutorado tutorado = this.iTutoradoRepository.findById(idTutorado)
                .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));
        return this.iGrupoTutoradoRepository.findByActivoAndTutorado(1, tutorado);
    }

    @Override
    public Map<Integer, Long> contarAlumnosPorGrupo() {
        List<Object[]> rows = this.iGrupoTutoradoRepository.countActivoByGrupo();
        Map<Integer, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((Integer) row[0], (Long) row[1]);
        }
        return map;
    }

    @Override
    public Page<GrupoTutorado> buscarHistorial(String q, Integer idSemestre, Integer idCarrera, Integer idGrupo, String estatusAcreditacion, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        String query = (q == null || q.isBlank()) ? null : q.trim();
        String estatus = (estatusAcreditacion == null || estatusAcreditacion.isBlank()) ? null : estatusAcreditacion.trim();
        return this.iGrupoTutoradoRepository.buscarHistorial(query, idSemestre, idCarrera, idGrupo, estatus, pageable);
    }

    @Override
    public List<Tutorado> obtenerTutoradosDisponibles(Integer idGrupo) {
        Grupo grupo = this.iGrupoRepository.findById(idGrupo)
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado"));

        if (grupo.getCarrera() == null) {
            return List.of();
        }

        Integer idSemestre = grupo.getSemestre() != null ? grupo.getSemestre().getId() : null;

        return this.iGrupoTutoradoRepository.findTutoradosDisponibles(
                grupo.getCarrera().getId(),
                idGrupo,
                idSemestre);
    }
}
