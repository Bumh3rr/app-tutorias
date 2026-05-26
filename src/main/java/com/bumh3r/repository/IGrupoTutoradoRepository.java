package com.bumh3r.repository;

import com.bumh3r.entity.Grupo;
import com.bumh3r.entity.GrupoTutorado;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutorado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IGrupoTutoradoRepository extends JpaRepository<GrupoTutorado, Integer> {

    List<GrupoTutorado> findByActivo(Integer activo);

    List<GrupoTutorado> findByActivoAndGrupo(Integer activo, Grupo grupo);

    List<GrupoTutorado> findByActivoAndTutorado(Integer activo, Tutorado tutorado);

    boolean existsByGrupoAndTutoradoAndActivo(Grupo grupo, Tutorado tutorado, Integer activo);

    long countByTutoradoAndActivo(Tutorado tutorado, Integer activo);

    long countByGrupoAndActivo(Grupo grupo, Integer activo);

    @Query("SELECT gt FROM GrupoTutorado gt WHERE gt.tutorado = :tutorado AND gt.grupo.semestre = :semestre AND gt.activo = :activo")
    List<GrupoTutorado> findByTutoradoAndGrupoSemestreAndActivo(
            @Param("tutorado") Tutorado tutorado,
            @Param("semestre") Semestre semestre,
            @Param("activo") Integer activo);

    @Query("SELECT gt FROM GrupoTutorado gt WHERE gt.grupo = :grupo AND (gt.activo IS NULL OR gt.activo = 1)")
    List<GrupoTutorado> findActiveByGrupo(@Param("grupo") Grupo grupo);

    @Query("SELECT gt.grupo.id, COUNT(gt) FROM GrupoTutorado gt WHERE gt.activo = 1 GROUP BY gt.grupo.id")
    List<Object[]> countActivoByGrupo();

    @Query(value = """
        SELECT gt.*
        FROM grupo_tutorado gt
        JOIN tutorado t ON t.id = gt.id_tutorado
        JOIN grupo    g ON g.id = gt.id_grupo
        WHERE (
            :q IS NULL
            OR LOWER(t.nombre)         LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(t.apellido)       LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(t.numero_control) LIKE LOWER(CONCAT('%', :q, '%'))
        )
        AND (:idSemestre IS NULL OR g.id_semestre = :idSemestre)
        AND (:idCarrera  IS NULL OR g.id_carrera  = :idCarrera)
        AND (:idGrupo    IS NULL OR gt.id_grupo   = :idGrupo)
        AND (
            :estatusAcreditacion IS NULL
            OR (
                :estatusAcreditacion = 'acreditado'
                AND COALESCE(
                    (SELECT COUNT(*) FROM asistencia a
                     WHERE a.id_tutorado = gt.id_tutorado
                       AND (a.presente = 1 OR a.recuperada = 1)
                    ) * 100.0 / NULLIF(
                        (SELECT COUNT(*) FROM sesion s
                         JOIN grupo_tutorado gt2
                              ON gt2.id_grupo = s.id_grupo
                             AND gt2.id_tutorado = gt.id_tutorado
                             AND gt2.activo = 1
                         WHERE s.estatus_registro IN ('REALIZADA', 'PENDIENTE')
                        ), 0),
                    0) >= 80
            )
            OR (
                :estatusAcreditacion = 'no_acreditado'
                AND COALESCE(
                    (SELECT COUNT(*) FROM asistencia a
                     WHERE a.id_tutorado = gt.id_tutorado
                       AND (a.presente = 1 OR a.recuperada = 1)
                    ) * 100.0 / NULLIF(
                        (SELECT COUNT(*) FROM sesion s
                         JOIN grupo_tutorado gt2
                              ON gt2.id_grupo = s.id_grupo
                             AND gt2.id_tutorado = gt.id_tutorado
                             AND gt2.activo = 1
                         WHERE s.estatus_registro IN ('REALIZADA', 'PENDIENTE')
                        ), 0),
                    0) < 80
            )
        )
        ORDER BY gt.id DESC
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM grupo_tutorado gt
        JOIN tutorado t ON t.id = gt.id_tutorado
        JOIN grupo    g ON g.id = gt.id_grupo
        WHERE (
            :q IS NULL
            OR LOWER(t.nombre)         LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(t.apellido)       LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(t.numero_control) LIKE LOWER(CONCAT('%', :q, '%'))
        )
        AND (:idSemestre IS NULL OR g.id_semestre = :idSemestre)
        AND (:idCarrera  IS NULL OR g.id_carrera  = :idCarrera)
        AND (:idGrupo    IS NULL OR gt.id_grupo   = :idGrupo)
        AND (
            :estatusAcreditacion IS NULL
            OR (
                :estatusAcreditacion = 'acreditado'
                AND COALESCE(
                    (SELECT COUNT(*) FROM asistencia a
                     WHERE a.id_tutorado = gt.id_tutorado
                       AND (a.presente = 1 OR a.recuperada = 1)
                    ) * 100.0 / NULLIF(
                        (SELECT COUNT(*) FROM sesion s
                         JOIN grupo_tutorado gt2
                              ON gt2.id_grupo = s.id_grupo
                             AND gt2.id_tutorado = gt.id_tutorado
                             AND gt2.activo = 1
                         WHERE s.estatus_registro IN ('REALIZADA', 'PENDIENTE')
                        ), 0),
                    0) >= 80
            )
            OR (
                :estatusAcreditacion = 'no_acreditado'
                AND COALESCE(
                    (SELECT COUNT(*) FROM asistencia a
                     WHERE a.id_tutorado = gt.id_tutorado
                       AND (a.presente = 1 OR a.recuperada = 1)
                    ) * 100.0 / NULLIF(
                        (SELECT COUNT(*) FROM sesion s
                         JOIN grupo_tutorado gt2
                              ON gt2.id_grupo = s.id_grupo
                             AND gt2.id_tutorado = gt.id_tutorado
                             AND gt2.activo = 1
                         WHERE s.estatus_registro IN ('REALIZADA', 'PENDIENTE')
                        ), 0),
                    0) < 80
            )
        )
        """,
        nativeQuery = true)
    Page<GrupoTutorado> buscarHistorial(
            @Param("q") String q,
            @Param("idSemestre") Integer idSemestre,
            @Param("idCarrera") Integer idCarrera,
            @Param("idGrupo") Integer idGrupo,
            @Param("estatusAcreditacion") String estatusAcreditacion,
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
        AND (:idSemestre IS NULL OR t.id NOT IN (
            SELECT gt2.tutorado.id FROM GrupoTutorado gt2
            WHERE gt2.grupo.semestre.id = :idSemestre AND gt2.activo = 1))
    """)
    List<Tutorado> findTutoradosDisponibles(
            @Param("idCarrera") Integer idCarrera,
            @Param("idGrupo") Integer idGrupo,
            @Param("idSemestre") Integer idSemestre);
}
