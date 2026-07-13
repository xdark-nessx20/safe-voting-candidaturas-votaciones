package com.safevoting.elecciones.domain.exception.candidatura;

import java.util.UUID;

public class VotacionNoActivaException extends RuntimeException {
    private final String errorCode = "VOTACION_NO_ACTIVA";

    public VotacionNoActivaException(UUID votacionId) {
        super("La votación " + votacionId + " no está en estado ACTIVA");
    }

    public String getErrorCode() { return errorCode; }
}
