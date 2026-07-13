package com.safevoting.elecciones.domain.exception.participacion;

import java.util.UUID;

public class ParticipacionNoEncontradaException extends RuntimeException {
    private final String errorCode = "PARTICIPACION_NO_ENCONTRADA";

    public ParticipacionNoEncontradaException(UUID id) {
        super("Participación no encontrada con ID: " + id);
    }

    public String getErrorCode() { return errorCode; }
}
