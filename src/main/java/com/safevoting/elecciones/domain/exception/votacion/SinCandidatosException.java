package com.safevoting.elecciones.domain.exception.votacion;

public class SinCandidatosException extends RuntimeException {
    private final String errorCode = "SIN_CANDIDATOS";

    public SinCandidatosException() {
        super("La votación no puede abrirse sin candidatos asignados");
    }

    public String getErrorCode() {
        return errorCode;
    }
}
