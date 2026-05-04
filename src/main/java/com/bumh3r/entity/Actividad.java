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
@Table(name = "actividad")
public class Actividad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ0-9 .,;:()'\\-]+$", message = "El nombre contiene caracteres no permitidos")
    private String nombre;

    private String descripcion;

    @NotNull(message = "La fecha es obligatoria")
    private Date fecha;

    @NotNull(message = "La semana es obligatoria")
    @Min(value = 1, message = "La semana debe estar entre 1 y 10")
    @Max(value = 10, message = "La semana debe estar entre 1 y 10")
    private Integer semana;

    private String foto;

    @ManyToOne
    @JoinColumn(name = "id_pat")
    private PAT pat;

    private Integer activo;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;
}
