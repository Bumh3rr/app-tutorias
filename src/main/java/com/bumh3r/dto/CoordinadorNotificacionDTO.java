package com.bumh3r.dto;

import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.CoordinadorCarrera;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CoordinadorNotificacionDTO {
    private CoordinadorCarrera coordinador;
    private int totalActividades;
    private List<Actividad> actividadesAplicables;
    private boolean puedeRecibirCorreo;
}
