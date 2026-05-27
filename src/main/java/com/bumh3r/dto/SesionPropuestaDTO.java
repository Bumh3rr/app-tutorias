package com.bumh3r.dto;

import java.util.ArrayList;
import java.util.List;

public class SesionPropuestaDTO {
    private Integer semana;
    private String fecha;
    private String diaSemana;
    private String nombreActividad;
    private Integer idActividad;
    private String origenPat;  // "GENERAL", "CARRERA", or null
    private String patNombre;
    private List<ConflictoDTO> conflictos = new ArrayList<>();

    public Integer getSemana() { return semana; }
    public void setSemana(Integer semana) { this.semana = semana; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }

    public String getNombreActividad() { return nombreActividad; }
    public void setNombreActividad(String nombreActividad) { this.nombreActividad = nombreActividad; }

    public Integer getIdActividad() { return idActividad; }
    public void setIdActividad(Integer idActividad) { this.idActividad = idActividad; }

    public String getOrigenPat() { return origenPat; }
    public void setOrigenPat(String origenPat) { this.origenPat = origenPat; }

    public String getPatNombre() { return patNombre; }
    public void setPatNombre(String patNombre) { this.patNombre = patNombre; }

    public List<ConflictoDTO> getConflictos() { return conflictos; }
    public void setConflictos(List<ConflictoDTO> conflictos) { this.conflictos = conflictos; }

    public boolean isTieneConflictos() { return !conflictos.isEmpty(); }
}
