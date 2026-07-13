package com.safevoting.elecciones.domain.exception.voto;

import java.util.UUID;

public class VotoNoEncontradoException extends RuntimeException {
    private final String errorCode = "VOTO_NO_ENCONTRADO";

    public VotoNoEncontradoException(UUID id) {
        super("Voto no encontrado con ID: " + id);
    }

    public String getErrorCode() { return errorCode; }
}
