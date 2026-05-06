package com.bumh3r.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PublicDeteccionForm {
    private Integer idTutorado;
    private Integer idSesion;
    private String sexo;

    private Integer necesidadAlgebra;
    private Integer necesidadCalculo;
    private Integer necesidadDerecho;
    private String necesidadOtra;

    private Integer necesidadEconomica;
    private Integer necesidadPsicologica;
    private String temaPsicologico;

    private Integer tieneBeca;
    private String nombreBeca;
    private Integer tieneEscasezMateriales;
    private String materialesRequeridos;

    private Integer tieneAtencionMedica;
    private String especificacionMedica;

    private Integer tieneVinculacionFamilia;
    private String razonVinculacion;

    private String observaciones;
}
