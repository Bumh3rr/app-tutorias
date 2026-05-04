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

    private String observaciones;

    @Column(name = "fecha_aplicacion")
    private Date fechaAplicacion;

    private Integer activo;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;
}