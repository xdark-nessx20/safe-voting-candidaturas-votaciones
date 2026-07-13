package com.safevoting.elecciones.domain.model.candidatura;

import com.safevoting.elecciones.domain.exception.candidatura.CandidaturaYaCanceladaException;
import com.safevoting.elecciones.domain.exception.DatosInvalidosException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidatura {

    private UUID id;
    private UUID miembroPartidoId;
    private UUID partidoId;
    private UUID votacionId;
    @Builder.Default
    private Instant fechaInscripcion = Instant.now();
    @Builder.Default
    private EstadoCandidatura estado = EstadoCandidatura.ACTIVA;

    public boolean isActiva() {
        return EstadoCandidatura.ACTIVA.equals(this.estado);
    }

    public boolean isCancelada(){
        return EstadoCandidatura.CANCELADA.equals(this.estado);
    }

    public void validateInfo() {
        if (miembroPartidoId == null || partidoId == null || votacionId == null) {
            throw new DatosInvalidosException("miembroPartidoId, partidoId y votacionId son obligatorios");
        }
    }

    public void cancelar() {
        if (isCancelada()) {
            throw new CandidaturaYaCanceladaException(this.id);
        }
        this.estado = EstadoCandidatura.CANCELADA;
    }
}
