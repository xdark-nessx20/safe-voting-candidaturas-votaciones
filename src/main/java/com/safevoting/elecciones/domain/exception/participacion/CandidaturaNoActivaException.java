package com.safevoting.elecciones.domain.exception.participacion;

import java.util.UUID;

public class CandidaturaNoActivaException extends RuntimeException {
    private final String errorCode = "CANDIDATURA_NO_ACTIVA";

    public CandidaturaNoActivaException(UUID candidaturaId) {
        super("La candidatura " + candidaturaId + " no está ACTIVA");
    }

    public String getErrorCode() { return errorCode; }
}
