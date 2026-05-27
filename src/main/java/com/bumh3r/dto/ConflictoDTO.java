package com.bumh3r.dto;

public class ConflictoDTO {
    private String tipo;
    private String descripcion;

    public ConflictoDTO(String tipo, String descripcion) {
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    public String getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
}
