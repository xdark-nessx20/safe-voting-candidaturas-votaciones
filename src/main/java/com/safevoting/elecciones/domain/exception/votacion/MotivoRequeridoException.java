package com.safevoting.elecciones.domain.exception.votacion;

public class MotivoRequeridoException extends RuntimeException {
    private final String errorCode = "MOTIVO_REQUERIDO";

    public MotivoRequeridoException() {
        super("El motivo es obligatorio para esta operación");
    }

    public String getErrorCode() {
        return errorCode;
    }
}
