package com.bumh3r.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "tutor")
public class Tutor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String apellido;

    @Column(name = "numero_control")
    private String numeroControl;

    private String email;
    private String foto;
    private String aula;
    private String horario;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana")
    private DiaSemana diaSemana;

    @ManyToOne
    @JoinColumn(name = "id_carrera")
    private Carrera carrera;

    @ManyToOne
    @JoinColumn(name = "id_semestre")
    private Semestre semestre;

    private Integer activo;

    public enum DiaSemana {
        LUNES("Lunes"),
        MARTES("Martes"),
        MIERCOLES("Miércoles"),
        JUEVES("Jueves"),
        VIERNES("Viernes");

        private String nombre;

        DiaSemana(String nombre) {
            this.nombre = nombre;
        }
        public String getNombre() {
            return nombre;
        }

        @Override
        public String toString() {
            return this.nombre;
        }
    }

}