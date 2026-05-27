package com.bumh3r.dto;

import java.util.ArrayList;
import java.util.List;

public class PreviewGeneracionDTO {
    // Core
    private Integer idGrupo;
    private String nombreGrupo;
    private String error;
    private List<SesionPropuestaDTO> sesiones = new ArrayList<>();
    private int totalConflictos;
    private boolean puedoGenerar;

    // Group info for display
    private String tutorNombre;
    private String aula;
    private String horario;
    private String diaSemanaGrupo;
    private String carreraNombre;

    // PAT selection
    private List<PatDisponibleDTO> patsDisponibles = new ArrayList<>();
    private List<Integer> idsPatsUsados = new ArrayList<>();

    // Summary counts
    private int totalConActividad;
    private int totalSinActividad;

    // DIA_SEMANA global suggestion
    private boolean tieneDiaSemanaConflicto;
    private String fechaSugeridaGlobal;  // ISO date string
    private String diaSugeridoDisplay;   // e.g. "Viernes"

    public Integer getIdGrupo() { return idGrupo; }
    public void setIdGrupo(Integer idGrupo) { this.idGrupo = idGrupo; }

    public String getNombreGrupo() { return nombreGrupo; }
    public void setNombreGrupo(String nombreGrupo) { this.nombreGrupo = nombreGrupo; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public List<SesionPropuestaDTO> getSesiones() { return sesiones; }
    public void setSesiones(List<SesionPropuestaDTO> sesiones) { this.sesiones = sesiones; }

    public int getTotalConflictos() { return totalConflictos; }
    public void setTotalConflictos(int totalConflictos) { this.totalConflictos = totalConflictos; }

    public boolean isPuedoGenerar() { return puedoGenerar; }
    public void setPuedoGenerar(boolean puedoGenerar) { this.puedoGenerar = puedoGenerar; }

    public String getTutorNombre() { return tutorNombre; }
    public void setTutorNombre(String tutorNombre) { this.tutorNombre = tutorNombre; }

    public String getAula() { return aula; }
    public void setAula(String aula) { this.aula = aula; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getDiaSemanaGrupo() { return diaSemanaGrupo; }
    public void setDiaSemanaGrupo(String diaSemanaGrupo) { this.diaSemanaGrupo = diaSemanaGrupo; }

    public String getCarreraNombre() { return carreraNombre; }
    public void setCarreraNombre(String carreraNombre) { this.carreraNombre = carreraNombre; }

    public List<PatDisponibleDTO> getPatsDisponibles() { return patsDisponibles; }
    public void setPatsDisponibles(List<PatDisponibleDTO> patsDisponibles) { this.patsDisponibles = patsDisponibles; }

    public List<Integer> getIdsPatsUsados() { return idsPatsUsados; }
    public void setIdsPatsUsados(List<Integer> idsPatsUsados) { this.idsPatsUsados = idsPatsUsados; }

    public int getTotalConActividad() { return totalConActividad; }
    public void setTotalConActividad(int totalConActividad) { this.totalConActividad = totalConActividad; }

    public int getTotalSinActividad() { return totalSinActividad; }
    public void setTotalSinActividad(int totalSinActividad) { this.totalSinActividad = totalSinActividad; }

    public boolean isTieneDiaSemanaConflicto() { return tieneDiaSemanaConflicto; }
    public void setTieneDiaSemanaConflicto(boolean tieneDiaSemanaConflicto) { this.tieneDiaSemanaConflicto = tieneDiaSemanaConflicto; }

    public String getFechaSugeridaGlobal() { return fechaSugeridaGlobal; }
    public void setFechaSugeridaGlobal(String fechaSugeridaGlobal) { this.fechaSugeridaGlobal = fechaSugeridaGlobal; }

    public String getDiaSugeridoDisplay() { return diaSugeridoDisplay; }
    public void setDiaSugeridoDisplay(String diaSugeridoDisplay) { this.diaSugeridoDisplay = diaSugeridoDisplay; }
}
