package com.bumh3r.dto;

public class PatDisponibleDTO {
    private Integer id;
    private String nombre;
    private String tipo; // "GENERAL" or "CARRERA"
    private String nombreCarrera;
    private int totalActividades;
    private boolean seleccionadoPorDefecto;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getNombreCarrera() { return nombreCarrera; }
    public void setNombreCarrera(String nombreCarrera) { this.nombreCarrera = nombreCarrera; }

    public int getTotalActividades() { return totalActividades; }
    public void setTotalActividades(int totalActividades) { this.totalActividades = totalActividades; }

    public boolean isSeleccionadoPorDefecto() { return seleccionadoPorDefecto; }
    public void setSeleccionadoPorDefecto(boolean seleccionadoPorDefecto) { this.seleccionadoPorDefecto = seleccionadoPorDefecto; }
}
