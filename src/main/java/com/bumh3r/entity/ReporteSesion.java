package com.bumh3r.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "reporte_sesion")
public class ReporteSesion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "id_sesion")
    private Sesion sesion;

    @Column(name = "descripcion_actividad", columnDefinition = "TEXT")
    private String descripcionActividad;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "alumnos_presentes")
    private Integer alumnosPresentes;

    @Column(name = "fecha_entrega")
    private Date fechaEntrega;

    @Column(name = "estatus_revision")
    private String estatusRevision;

    private Integer activo;
}