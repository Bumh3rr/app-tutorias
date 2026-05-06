package com.bumh3r.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "deteccion_necesidades")
public class DeteccionNecesidades {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_tutorado")
    private Tutorado tutorado;

    @ManyToOne
    @JoinColumn(name = "id_sesion")
    private Sesion sesion;

    @Column(name = "necesidad_algebra")
    private Integer necesidadAlgebra;

    @Column(name = "necesidad_calculo")
    private Integer necesidadCalculo;

    @Column(name = "necesidad_derecho")
    private Integer necesidadDerecho;

    @Column(name = "necesidad_otra")
    private String necesidadOtra;

    @Column(name = "necesidad_economica")
    private Integer necesidadEconomica;

    @Column(name = "necesidad_psicologica")
    private Integer necesidadPsicologica;

    @Column(name = "tiene_beca")
    private Integer tieneBeca;

    @Column(name = "nombre_beca")
    private String nombreBeca;

    @Column(name = "tiene_escasez_materiales")
    private Integer tieneEscasezMateriales;

    @Column(name = "materiales_requeridos")
    private String materialesRequeridos;

    @Column(name = "tiene_atencion_medica")
    private Integer tieneAtencionMedica;

    @Column(name = "especificacion_medica")
    private String especificacionMedica;

    @Column(name = "tiene_vinculacion_familia")
    private Integer tieneVinculacionFamilia;

    @Column(name = "razon_vinculacion")
    private String razonVinculacion;

    @Column(name = "tema_psicologico")
    private String temaPsicologico;

    private String observaciones;

    private Integer activo;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;
}