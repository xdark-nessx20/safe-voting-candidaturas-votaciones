package com.safevoting.elecciones.domain.exception.participacion;

import java.util.UUID;

public class UsuarioYaVotoException extends RuntimeException {
    private final String errorCode = "USUARIO_YA_VOTO";

    public UsuarioYaVotoException(UUID usuarioId, UUID votacionId) {
        super("El usuario " + usuarioId + " ya votó en la votación " + votacionId);
    }

    public String getErrorCode() { return errorCode; }
}
