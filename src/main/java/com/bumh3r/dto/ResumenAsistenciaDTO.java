package com.bumh3r.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResumenAsistenciaDTO {
    private Integer idTutorado;
    private String nombreTutorado;
    private long totalSesiones;
    private long asistenciasPresente;
    private long asistenciasRecuperadas;
    private long totalAcreditadas;
    private double porcentaje;
    private boolean acreditado;
}