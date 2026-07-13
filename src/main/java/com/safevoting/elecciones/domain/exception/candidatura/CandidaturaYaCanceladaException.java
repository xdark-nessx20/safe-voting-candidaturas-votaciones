package com.safevoting.elecciones.domain.exception.candidatura;

import java.util.UUID;

public class CandidaturaYaCanceladaException extends RuntimeException {
    private final String errorCode = "CANDIDATURA_YA_CANCELADA";

    public CandidaturaYaCanceladaException(UUID id) {
        super("La candidatura " + id + " ya está cancelada");
    }

    public String getErrorCode() { return errorCode; }
}
