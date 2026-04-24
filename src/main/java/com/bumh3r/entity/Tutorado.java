package com.bumh3r.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "tutorado")
public class Tutorado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String apellido;

    @Column(name = "numero_control")
    private String numeroControl;

    private String email;
    private String foto;

    @ManyToOne
    @JoinColumn(name = "id_carrera")
    private Carrera carrera;

    @ManyToOne
    @JoinColumn(name = "id_semestre")
    private Semestre semestre;

    @Column(name = "grado")
    private Integer grado; // 1 = primer semestre de carrera, 2 = segundo semestre

    private Integer activo;
}