package com.bumh3r.service.impl;

import com.bumh3r.dto.ConflictoDTO;
import com.bumh3r.dto.PatDisponibleDTO;
import com.bumh3r.dto.PreviewGeneracionDTO;
import com.bumh3r.dto.SesionPropuestaDTO;
import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.Grupo;
import com.bumh3r.entity.PAT;
import com.bumh3r.entity.Sesion;
import com.bumh3r.repository.IActividadRepository;
import com.bumh3r.repository.IGrupoRepository;
import com.bumh3r.repository.IPATRepository;
import com.bumh3r.repository.ISesionRepository;
import com.bumh3r.service.SesionGeneracionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SesionGeneracionServiceImpl implements SesionGeneracionService {

    private static final Logger log = LoggerFactory.getLogger(SesionGeneracionServiceImpl.class);

    private static final Map<String, DayOfWeek> DIA_MAP = Map.of(
            "LUNES", DayOfWeek.MONDAY,
            "MARTES", DayOfWeek.TUESDAY,
            "MIERCOLES", DayOfWeek.WEDNESDAY,
            "MIÉRCOLES", DayOfWeek.WEDNESDAY,
            "JUEVES", DayOfWeek.THURSDAY,
            "VIERNES", DayOfWeek.FRIDAY,
            "SABADO", DayOfWeek.SATURDAY,
            "SÁBADO", DayOfWeek.SATURDAY,
            "DOMINGO", DayOfWeek.SUNDAY
    );

    private static final Map<DayOfWeek, String> DAY_DISPLAY = Map.of(
            DayOfWeek.MONDAY, "Lunes",
            DayOfWeek.TUESDAY, "Martes",
            DayOfWeek.WEDNESDAY, "Miércoles",
            DayOfWeek.THURSDAY, "Jueves",
            DayOfWeek.FRIDAY, "Viernes",
            DayOfWeek.SATURDAY, "Sábado",
            DayOfWeek.SUNDAY, "Domingo"
    );

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired private IGrupoRepository grupoRepository;
    @Autowired private ISesionRepository sesionRepository;
    @Autowired private IPATRepository patRepository;
    @Autowired private IActividadRepository actividadRepository;

    @Override
    public PreviewGeneracionDTO previewGeneracion(Integer idGrupo, LocalDate fechaInicio, List<Integer> idsPats) {
        PreviewGeneracionDTO preview = new PreviewGeneracionDTO();

        Optional<Grupo> opt = grupoRepository.findById(idGrupo);
        if (opt.isEmpty()) {
            preview.setError("Grupo no encontrado");
            return preview;
        }
        Grupo grupo = opt.get();
        preview.setIdGrupo(idGrupo);
        preview.setNombreGrupo(grupo.getNombre());
        preview.setAula(grupo.getAula());
        preview.setHorario(grupo.getHorario());
        preview.setDiaSemanaGrupo(grupo.getDiaSemana());
        preview.setCarreraNombre(grupo.getCarrera() != null ? grupo.getCarrera().getNombre() : null);

        if (grupo.getTutor() != null) {
            preview.setTutorNombre(grupo.getTutor().getNombre() + " " + grupo.getTutor().getApellido());
        } else {
            preview.setError("El grupo no tiene un tutor asignado. Asigna un tutor antes de generar sesiones.");
            return preview;
        }

        long countExistentes = sesionRepository.countByGrupoAndActivo(grupo, 1);
        if (countExistentes > 0) {
            preview.setError("Este grupo ya tiene " + countExistentes + " sesión(es) registrada(s). No se puede generar de nuevo.");
            return preview;
        }

        // ── Load all applicable PATs ──
        List<PAT> patsCarrera = new ArrayList<>();
        List<PAT> patsGenerales = new ArrayList<>();

        if (grupo.getCarrera() != null && grupo.getSemestre() != null) {
            patsCarrera = patRepository.findByActivoAndCarreraAndSemestre(1, grupo.getCarrera(), grupo.getSemestre());
        }
        if (grupo.getSemestre() != null) {
            patsGenerales = patRepository.findGeneralesBySemestre(grupo.getSemestre());
        }

        // Build PatDisponibleDTO list (carrera first, then general)
        List<PatDisponibleDTO> patsDisponibles = new ArrayList<>();
        for (PAT pat : patsCarrera) {
            PatDisponibleDTO dto = new PatDisponibleDTO();
            dto.setId(pat.getId());
            dto.setNombre(pat.getNombre());
            dto.setTipo("CARRERA");
            dto.setNombreCarrera(grupo.getCarrera().getNombre());
            dto.setTotalActividades(actividadRepository.findByActivoAndPat(1, pat).size());
            dto.setSeleccionadoPorDefecto(true);
            patsDisponibles.add(dto);
        }
        for (PAT pat : patsGenerales) {
            PatDisponibleDTO dto = new PatDisponibleDTO();
            dto.setId(pat.getId());
            dto.setNombre(pat.getNombre());
            dto.setTipo("GENERAL");
            dto.setTotalActividades(actividadRepository.findByActivoAndPat(1, pat).size());
            dto.setSeleccionadoPorDefecto(true);
            patsDisponibles.add(dto);
        }
        preview.setPatsDisponibles(patsDisponibles);

        // ── Filter PATs by selection ──
        List<PAT> usadosOrdenados = resolverPATsOrdenados(patsCarrera, patsGenerales, idsPats);
        preview.setIdsPatsUsados(usadosOrdenados.stream().map(PAT::getId).collect(Collectors.toList()));

        // ── DIA_SEMANA global check ──
        DayOfWeek grupoDia = null;
        if (grupo.getDiaSemana() != null && !grupo.getDiaSemana().isBlank()) {
            grupoDia = DIA_MAP.get(grupo.getDiaSemana().toUpperCase().trim());
        }

        if (grupoDia != null && fechaInicio.getDayOfWeek() != grupoDia) {
            preview.setTieneDiaSemanaConflicto(true);
            int diasHasta = (grupoDia.getValue() - fechaInicio.getDayOfWeek().getValue() + 7) % 7;
            LocalDate sugerida = fechaInicio.plusDays(diasHasta);
            preview.setFechaSugeridaGlobal(sugerida.format(ISO));
            preview.setDiaSugeridoDisplay(DAY_DISPLAY.getOrDefault(sugerida.getDayOfWeek(), ""));
        }

        // ── Generate 10 session proposals ──
        int totalConflictos = 0;
        int totalConActividad = 0;
        int totalSinActividad = 0;

        for (int semana = 1; semana <= 10; semana++) {
            SesionPropuestaDTO propuesta = new SesionPropuestaDTO();
            propuesta.setSemana(semana);

            LocalDate fecha = fechaInicio.plusWeeks(semana - 1);
            propuesta.setFecha(fecha.format(ISO));
            propuesta.setDiaSemana(DAY_DISPLAY.getOrDefault(fecha.getDayOfWeek(), fecha.getDayOfWeek().name()));

            // Conflict: DIA_SEMANA
            if (grupoDia != null && fecha.getDayOfWeek() != grupoDia) {
                propuesta.getConflictos().add(new ConflictoDTO("DIA_SEMANA",
                        "El grupo se imparte los " + grupo.getDiaSemana()
                        + " pero la semana " + semana + " cae en " + propuesta.getDiaSemana()));
            }

            // Activity lookup: carrera-specific PAT first, then general
            Actividad actividadEncontrada = null;
            String origenPat = null;
            String patNombre = null;
            for (PAT pat : usadosOrdenados) {
                Optional<Actividad> actOpt = actividadRepository.findByActivoAndPatAndSemana(1, pat, semana);
                if (actOpt.isPresent()) {
                    actividadEncontrada = actOpt.get();
                    origenPat = (pat.getEsGeneral() != null && pat.getEsGeneral() == 1) ? "GENERAL" : "CARRERA";
                    patNombre = pat.getNombre();
                    break;
                }
            }

            if (actividadEncontrada != null) {
                propuesta.setNombreActividad(actividadEncontrada.getNombre());
                propuesta.setIdActividad(actividadEncontrada.getId());
                propuesta.setOrigenPat(origenPat);
                propuesta.setPatNombre(patNombre);
                totalConActividad++;
            } else {
                propuesta.getConflictos().add(new ConflictoDTO("SIN_ACTIVIDAD",
                        usadosOrdenados.isEmpty()
                                ? "No se seleccionó ningún PAT"
                                : "No hay actividad en los PATs seleccionados para la semana " + semana));
                totalSinActividad++;
            }

            // Conflict: AULA_OCUPADA
            if (grupo.getAula() != null && !grupo.getAula().isBlank()) {
                Date[] range = dayRange(fecha);
                if (sesionRepository.existsByAulaAndFechaRange(grupo.getAula(), range[0], range[1], idGrupo)) {
                    propuesta.getConflictos().add(new ConflictoDTO("AULA_OCUPADA",
                            "El aula " + grupo.getAula() + " ya tiene otra sesión el " + fecha.format(FMT)));
                }
            }

            // Conflict: TUTOR_OCUPADO
            Date[] range = dayRange(fecha);
            if (sesionRepository.existsByTutorAndFechaRange(grupo.getTutor().getId(), range[0], range[1], idGrupo)) {
                propuesta.getConflictos().add(new ConflictoDTO("TUTOR_OCUPADO",
                        "El tutor ya tiene otra sesión el " + fecha.format(FMT)));
            }

            totalConflictos += propuesta.getConflictos().size();
            preview.getSesiones().add(propuesta);
        }

        preview.setTotalConflictos(totalConflictos);
        preview.setTotalConActividad(totalConActividad);
        preview.setTotalSinActividad(totalSinActividad);
        preview.setPuedoGenerar(true);
        return preview;
    }

    @Override
    @Transactional
    public void generarSesiones(Integer idGrupo, LocalDate fechaInicio, List<Integer> idsPats) {
        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + idGrupo));

        if (grupo.getTutor() == null) {
            throw new IllegalStateException("El grupo no tiene un tutor asignado");
        }
        long countExistentes = sesionRepository.countByGrupoAndActivo(grupo, 1);
        if (countExistentes > 0) {
            throw new IllegalStateException("El grupo ya tiene sesiones registradas");
        }

        List<PAT> patsCarrera = new ArrayList<>();
        List<PAT> patsGenerales = new ArrayList<>();
        if (grupo.getCarrera() != null && grupo.getSemestre() != null) {
            patsCarrera = patRepository.findByActivoAndCarreraAndSemestre(1, grupo.getCarrera(), grupo.getSemestre());
        }
        if (grupo.getSemestre() != null) {
            patsGenerales = patRepository.findGeneralesBySemestre(grupo.getSemestre());
        }
        List<PAT> usadosOrdenados = resolverPATsOrdenados(patsCarrera, patsGenerales, idsPats);

        for (int semana = 1; semana <= 10; semana++) {
            LocalDate fecha = fechaInicio.plusWeeks(semana - 1);
            Date fechaDate = Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Actividad actividad = null;
            for (PAT pat : usadosOrdenados) {
                Optional<Actividad> actOpt = actividadRepository.findByActivoAndPatAndSemana(1, pat, semana);
                if (actOpt.isPresent()) {
                    actividad = actOpt.get();
                    break;
                }
            }

            sesionRepository.save(Sesion.builder()
                    .grupo(grupo)
                    .actividad(actividad)
                    .semana(semana)
                    .fechaImparticion(fechaDate)
                    .estatusRegistro("PENDIENTE")
                    .activo(1)
                    .build());
        }
    }

    // Carrera PATs first, then general — filtered by idsPats if non-empty
    private List<PAT> resolverPATsOrdenados(List<PAT> patsCarrera, List<PAT> patsGenerales, List<Integer> idsPats) {
        List<PAT> todos = new ArrayList<>(patsCarrera);
        todos.addAll(patsGenerales);
        if (idsPats == null || idsPats.isEmpty()) {
            return todos;
        }
        Set<Integer> seleccionados = new HashSet<>(idsPats);
        List<PAT> filtrados = todos.stream()
                .filter(p -> seleccionados.contains(p.getId()))
                .collect(Collectors.toList());
        if (filtrados.isEmpty()) {
            log.warn("Ninguno de los PATs seleccionados {} es aplicable al grupo. Usando todos.", idsPats);
            return todos;
        }
        return filtrados;
    }

    private Date[] dayRange(LocalDate date) {
        Date start = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        return new Date[]{start, end};
    }
}
