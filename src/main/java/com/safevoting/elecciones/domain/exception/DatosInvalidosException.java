package com.safevoting.elecciones.domain.exception;

public class DatosInvalidosException extends RuntimeException {

    private final String errorCode;

    public DatosInvalidosException(String message) {
        super(message);
        this.errorCode = "DATOS_INVALIDOS";
    }

    public DatosInvalidosException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
