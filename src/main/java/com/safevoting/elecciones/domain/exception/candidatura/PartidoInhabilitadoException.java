package com.safevoting.elecciones.domain.exception.candidatura;

import java.util.UUID;

public class PartidoInhabilitadoException extends RuntimeException {
    private final String errorCode = "PARTIDO_INHABILITADO";

    public PartidoInhabilitadoException(UUID partidoId) {
        super("El partido " + partidoId + " está inhabilitado");
    }

    public String getErrorCode() { return errorCode; }
}
