package com.safevoting.elecciones.domain.exception.votacion;

public class NombreDuplicadoException extends RuntimeException {
    private final String errorCode = "NOMBRE_VOTACION_DUPLICADO";

    public NombreDuplicadoException(String nombre) {
        super("Ya existe una votación con el nombre: " + nombre);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
