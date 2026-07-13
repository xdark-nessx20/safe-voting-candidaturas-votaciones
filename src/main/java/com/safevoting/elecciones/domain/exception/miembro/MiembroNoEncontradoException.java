package com.safevoting.elecciones.domain.exception.miembro;

import java.util.UUID;

public class MiembroNoEncontradoException extends RuntimeException {

    private final String errorCode = "MIEMBRO_NO_ENCONTRADO";

    public MiembroNoEncontradoException(UUID id) {
        super("Miembro no encontrado con ID: " + id);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
