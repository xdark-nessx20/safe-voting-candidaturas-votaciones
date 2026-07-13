package com.safevoting.elecciones.domain.exception.votacion;

import java.util.UUID;

public class VotacionNoEncontradaException extends RuntimeException {
    private final String errorCode = "VOTACION_NO_ENCONTRADA";

    public VotacionNoEncontradaException(UUID id) {
        super("Votación no encontrada con ID: " + id);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
