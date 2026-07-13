package com.safevoting.elecciones.domain.exception.candidatura;

import java.util.UUID;

public class PartidoNoCoincideException extends RuntimeException {
    private final String errorCode = "PARTIDO_NO_COINCIDE";

    public PartidoNoCoincideException(UUID miembroPartidoId, UUID partidoId) {
        super("El partidoId no coincide con el partido del miembro " + miembroPartidoId);
    }

    public String getErrorCode() { return errorCode; }
}
