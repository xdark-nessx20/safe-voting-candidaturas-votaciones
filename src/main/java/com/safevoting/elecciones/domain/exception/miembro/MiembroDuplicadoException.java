package com.safevoting.elecciones.domain.exception.miembro;

import java.util.UUID;

public class MiembroDuplicadoException extends RuntimeException {

    private final String errorCode = "MIEMBRO_DUPLICADO";

    public MiembroDuplicadoException(UUID usuarioId, UUID partidoId) {
        super("El usuario " + usuarioId + " ya es miembro del partido " + partidoId);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
