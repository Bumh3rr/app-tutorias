package com.bumh3r.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "asignacion_tutorado")
public class AsignacionTutorado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_tutor")
    private Tutor tutor;

    @ManyToOne
    @JoinColumn(name = "id_tutorado")
    private Tutorado tutorado;

    @ManyToOne
    @JoinColumn(name = "id_semestre")
    private Semestre semestre;

    private String foto;
    private Integer activo;
}