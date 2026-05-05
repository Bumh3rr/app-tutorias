package com.bumh3r.repository;

import com.bumh3r.entity.Grupo;
import com.bumh3r.entity.GrupoTutorado;
import com.bumh3r.entity.Tutorado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IGrupoTutoradoRepository extends JpaRepository<GrupoTutorado, Integer> {

    List<GrupoTutorado> findByActivo(Integer activo);

    List<GrupoTutorado> findByActivoAndGrupo(Integer activo, Grupo grupo);

    List<GrupoTutorado> findByActivoAndTutorado(Integer activo, Tutorado tutorado);

    boolean existsByGrupoAndTutoradoAndActivo(Grupo grupo, Tutorado tutorado, Integer activo);

    long countByTutoradoAndActivo(Tutorado tutorado, Integer activo);

    @Query("SELECT gt.grupo.id, COUNT(gt) FROM GrupoTutorado gt WHERE gt.activo = 1 GROUP BY gt.grupo.id")
    List<Object[]> countActivoByGrupo();

    @Query(value = """
        SELECT gt FROM GrupoTutorado gt
        WHERE (:q IS NULL OR :q = ''
               OR LOWER(gt.tutorado.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(gt.tutorado.apellido) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(gt.tutorado.numeroControl) LIKE LOWER(CONCAT('%', :q, '%')))
        AND (:idSemestre IS NULL OR gt.grupo.semestre.id = :idSemestre)
        AND (:idCarrera IS NULL OR gt.grupo.carrera.id = :idCarrera)
        """,
        countQuery = """
        SELECT COUNT(gt) FROM GrupoTutorado gt
        WHERE (:q IS NULL OR :q = ''
               OR LOWER(gt.tutorado.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(gt.tutorado.apellido) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(gt.tutorado.numeroControl) LIKE LOWER(CONCAT('%', :q, '%')))
        AND (:idSemestre IS NULL OR gt.grupo.semestre.id = :idSemestre)
        AND (:idCarrera IS NULL OR gt.grupo.carrera.id = :idCarrera)
        """)
    Page<GrupoTutorado> buscarHistorial(
            @Param("q") String q,
            @Param("idSemestre") Integer idSemestre,
            @Param("idCarrera") Integer idCarrera,
            Pageable pageable);

    @Query("""
        SELECT t FROM Tutorado t
        WHERE t.carrera.id = :idCarrera
        AND t.activo = 1
        AND (SELECT COUNT(gt) FROM GrupoTutorado gt
             WHERE gt.tutorado = t AND gt.activo = 1) < 2
        AND t.id NOT IN (
            SELECT gt.tutorado.id FROM GrupoTutorado gt
            WHERE gt.grupo.id = :idGrupo AND gt.activo = 1)
    """)
    List<Tutorado> findTutoradosDisponibles(
            @Param("idCarrera") Integer idCarrera,
            @Param("idGrupo") Integer idGrupo);
}
