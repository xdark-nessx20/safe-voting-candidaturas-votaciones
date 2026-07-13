package com.safevoting.elecciones.domain.exception.voto;

import java.util.UUID;

public class VotacionNoEnProgresoException extends RuntimeException {
    private final String errorCode = "VOTACION_NO_EN_PROGRESO";

    public VotacionNoEnProgresoException(UUID votacionId) {
        super("La votación " + votacionId + " no está en estado EN_PROGRESO");
    }

    public String getErrorCode() { return errorCode; }
}
