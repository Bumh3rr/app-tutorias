package com.bumh3r.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "sesion")
public class Sesion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_grupo")
    private Grupo grupo;

    @ManyToOne
    @JoinColumn(name = "id_actividad")
    private Actividad actividad;

    @NotNull(message = "La semana es obligatoria")
    @Min(value = 1, message = "La semana debe estar entre 1 y 10")
    @Max(value = 10, message = "La semana debe estar entre 1 y 10")
    private Integer semana;

    @NotNull(message = "La fecha de impartición es obligatoria")
    @Column(name = "fecha_imparticion")
    private Date fechaImparticion;

    @Column(name = "estatus_registro")
    private String estatusRegistro;

    private Integer activo;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;
}
