package com.safevoting.elecciones.domain.exception.miembro;

import java.util.UUID;

public class MiembroYaActivoException extends RuntimeException {
    private final String errorCode = "MIEMBRO_YA_ACTIVO";

    public MiembroYaActivoException(UUID id) {
        super("El miembro " + id + " ya se encuentra activo");
    }
}
