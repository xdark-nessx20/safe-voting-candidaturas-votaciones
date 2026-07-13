package com.safevoting.elecciones.domain.exception.miembro;

import lombok.Getter;

import java.util.UUID;

@Getter
public class MiembroYaInactivoException extends RuntimeException {

    private final String errorCode = "MIEMBRO_YA_INACTIVO";

    public MiembroYaInactivoException(UUID id) {
        super("El miembro " + id + " ya se encuentra inactivo");
    }

}
