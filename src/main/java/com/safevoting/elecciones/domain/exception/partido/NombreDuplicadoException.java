package com.safevoting.elecciones.domain.exception.partido;

import java.util.UUID;

public class NombreDuplicadoException extends RuntimeException {

    private final String errorCode;

    public NombreDuplicadoException(String nombre) {
        super("Ya existe un partido con el nombre: " + nombre);
        this.errorCode = "NOMBRE_DUPLICADO";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
