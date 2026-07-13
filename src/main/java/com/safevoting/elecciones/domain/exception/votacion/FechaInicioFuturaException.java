package com.safevoting.elecciones.domain.exception.votacion;

public class FechaInicioFuturaException extends RuntimeException {
    private final String errorCode = "FECHA_INICIO_FUTURA";

    public FechaInicioFuturaException() {
        super("No se puede abrir la votación antes de la fecha de inicio");
    }

    public String getErrorCode() {
        return errorCode;
    }
}
