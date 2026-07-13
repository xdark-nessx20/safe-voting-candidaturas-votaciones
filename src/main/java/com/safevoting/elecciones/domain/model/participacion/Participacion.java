package com.safevoting.elecciones.domain.model.participacion;

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
public class Participacion {

    private UUID id;
    private UUID usuarioId;
    private UUID votoId;
    private UUID votacionId;
    @Builder.Default
    private EstadoParticipacion estado = EstadoParticipacion.VALIDO;
    @Builder.Default
    private Instant fechaEmision = Instant.now();

    public boolean isValid() {
        return EstadoParticipacion.VALIDO.equals(this.estado);
    }

    public boolean isAnulado(){
        return EstadoParticipacion.ANULADO.equals(this.estado);
    }

    public void validateInfo() {
        if (usuarioId == null || votoId == null || votacionId == null) {
            throw new DatosInvalidosException("usuarioId, votoId y votacionId son obligatorios");
        }
    }
}
