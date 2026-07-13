package com.safevoting.elecciones.domain.exception.candidatura;

import java.util.UUID;

public class CandidaturaNoEncontradaException extends RuntimeException {
    private final String errorCode = "CANDIDATURA_NO_ENCONTRADA";

    public CandidaturaNoEncontradaException(UUID id) {
        super("Candidatura no encontrada con ID: " + id);
    }

    public String getErrorCode() { return errorCode; }
}
