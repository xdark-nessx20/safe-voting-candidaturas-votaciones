package com.safevoting.elecciones.domain.exception.candidatura;

import java.util.UUID;

public class MiembroNoActivoException extends RuntimeException {
    private final String errorCode = "MIEMBRO_NO_ACTIVO";

    public MiembroNoActivoException(UUID miembroPartidoId) {
        super("El miembro " + miembroPartidoId + " no está ACTIVO");
    }

    public String getErrorCode() { return errorCode; }
}
