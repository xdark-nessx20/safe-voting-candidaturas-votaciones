package com.safevoting.elecciones.domain.exception.participacion;

import java.util.UUID;

public class UsuarioNoHabilitadoException extends RuntimeException {
    private final String errorCode = "USUARIO_NO_HABILITADO";

    public UsuarioNoHabilitadoException(UUID usuarioId) {
        super("El usuario " + usuarioId + " no está habilitado para votar");
    }

    public String getErrorCode() { return errorCode; }
}
