package com.bumh3r.repository;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.Grupo;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IGrupoRepository extends JpaRepository<Grupo, Integer> {

    List<Grupo> findByActivo(Integer activo);

    List<Grupo> findByActivoAndTutor(Integer activo, Tutor tutor);

    List<Grupo> findByActivoAndSemestre(Integer activo, Semestre semestre);

    List<Grupo> findByActivoAndTutorAndSemestre(Integer activo, Tutor tutor, Semestre semestre);

    List<Grupo> findByActivoAndCarreraAndSemestre(Integer activo, Carrera carrera, Semestre semestre);

    Page<Grupo> findByActivo(Integer activo, Pageable pageable);
    Page<Grupo> findByActivoAndSemestre(Integer activo, Semestre semestre, Pageable pageable);
    Page<Grupo> findByActivoAndTutorAndSemestre(Integer activo, Tutor tutor, Semestre semestre, Pageable pageable);
    Page<Grupo> findByActivoAndCarreraAndSemestre(Integer activo, Carrera carrera, Semestre semestre, Pageable pageable);

    boolean existsByAulaAndDiaSemanaAndHorarioAndActivo(String aula, String diaSemana, String horario, Integer activo);

    boolean existsByAulaAndDiaSemanaAndHorarioAndActivoAndIdNot(String aula, String diaSemana, String horario, Integer activo, Integer id);

    @Query("SELECT COUNT(g) > 0 FROM Grupo g WHERE g.nombre = :nombre AND g.semestre.id = :idSemestre AND g.carrera.id = :idCarrera AND g.activo = 1")
    boolean existsByNombreAndSemestreAndCarreraAndActivo(@Param("nombre") String nombre, @Param("idSemestre") Integer idSemestre, @Param("idCarrera") Integer idCarrera);

    @Query("SELECT COUNT(g) > 0 FROM Grupo g WHERE g.nombre = :nombre AND g.semestre.id = :idSemestre AND g.carrera.id = :idCarrera AND g.activo = 1 AND g.id <> :id")
    boolean existsByNombreAndSemestreAndCarreraAndActivoExcludingId(@Param("nombre") String nombre, @Param("idSemestre") Integer idSemestre, @Param("idCarrera") Integer idCarrera, @Param("id") Integer id);

    @Query("SELECT g FROM Grupo g WHERE g.activo = 1 AND (:q IS NULL OR :q = '' OR LOWER(g.nombre) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Grupo> searchByName(@Param("q") String q, Pageable pageable);

    // Grupos sin tutor asignado
    Page<Grupo> findByActivoAndTutorIsNull(Integer activo, Pageable pageable);

    @Query("SELECT g FROM Grupo g WHERE g.activo = 1 AND g.tutor IS NULL AND (:q IS NULL OR :q = '' OR LOWER(g.nombre) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Grupo> searchSinTutorByName(@Param("q") String q, Pageable pageable);

    @Query("SELECT g FROM Grupo g WHERE g.activo = 1 AND g.tutor IS NULL AND g.semestre.id = :idSemestre")
    Page<Grupo> findSinTutorBySemestre(@Param("idSemestre") Integer idSemestre, Pageable pageable);

    @Query("SELECT g FROM Grupo g WHERE g.activo = 1 AND g.tutor IS NULL AND g.carrera.id = :idCarrera")
    Page<Grupo> findSinTutorByCarrera(@Param("idCarrera") Integer idCarrera, Pageable pageable);

    @Query("SELECT e FROM Grupo e WHERE e.activo = 1 AND e.fechaRegistro BETWEEN :inicio AND :fin")
    Page<Grupo> findByFechaRegistroRange(@Param("inicio") java.util.Date inicio, @Param("fin") java.util.Date fin, Pageable pageable);
}
