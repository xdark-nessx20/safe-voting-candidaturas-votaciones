package com.safevoting.elecciones.domain.exception.partido;

import java.util.UUID;

public class PartidoNoEncontradoException extends RuntimeException {

    private final String errorCode;

    public PartidoNoEncontradoException(UUID id) {
        super("Partido no encontrado con id: " + id);
        this.errorCode = "PARTIDO_NO_ENCONTRADO";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
