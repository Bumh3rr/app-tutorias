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
@Table(name = "evidencia_sesion")
public class EvidenciaSesion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_sesion")
    private Sesion sesion;

    @Column(name = "archivo_url")
    private String archivoUrl;

    @Column(name = "notas_coordinador")
    private String notasCoordinador;

    @Column(name = "estatus_validacion")
    private String estatusValidacion;

    @Column(name = "fecha_subida")
    private Date fechaSubida;

    private Integer activo;
}