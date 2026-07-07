package com.safevoting.elecciones.domain.exception.partido;

import java.util.UUID;

public class PartidoYaInhabilitadoException extends RuntimeException {

    private final String errorCode;

    public PartidoYaInhabilitadoException(UUID id) {
        super("El partido con id " + id + " ya se encuentra inhabilitado");
        this.errorCode = "PARTIDO_YA_INHABILITADO";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
