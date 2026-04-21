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
@Table(name = "sesion")
public class Sesion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_tutor")
    private Tutor tutor;

    @ManyToOne
    @JoinColumn(name = "id_actividad")
    private Actividad actividad;

    private Integer semana;

    @Column(name = "fecha_imparticion")
    private Date fechaImparticion;

    @Column(name = "estatus_registro")
    private String estatusRegistro;

    private Integer activo;
}