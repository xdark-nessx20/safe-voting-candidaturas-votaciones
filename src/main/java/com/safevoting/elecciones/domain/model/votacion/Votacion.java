package com.safevoting.elecciones.domain.model.votacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Votacion {

    private UUID id;
    private String nombre;
    private EstadoVotacion estado;

    public boolean isActiva() {
        return EstadoVotacion.ACTIVA.equals(this.estado);
    }

    public boolean isEnProgreso() {
        return EstadoVotacion.EN_PROGRESO.equals(this.estado);
    }

    public boolean isFinalizada() {
        return EstadoVotacion.FINALIZADA.equals(this.estado);
    }

    public boolean isCompletada() {
        return EstadoVotacion.COMPLETADA.equals(this.estado);
    }

    public boolean isCancelada() {
        return EstadoVotacion.CANCELADA.equals(this.estado);
    }
}
