package com.safevoting.elecciones.domain.exception.miembro;

import java.util.UUID;

public class UsuarioNoEncontradoException extends RuntimeException {

    private final String errorCode = "USUARIO_NO_ENCONTRADO";

    public UsuarioNoEncontradoException(UUID usuarioId) {
        super("Usuario no encontrado en el Módulo 1: " + usuarioId);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
