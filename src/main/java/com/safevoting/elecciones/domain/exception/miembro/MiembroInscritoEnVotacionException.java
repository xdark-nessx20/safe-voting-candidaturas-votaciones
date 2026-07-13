package com.safevoting.elecciones.domain.exception.miembro;

import java.util.UUID;

public class MiembroInscritoEnVotacionException extends RuntimeException {

    private final String errorCode = "MIEMBRO_INSCRITO_EN_VOTACION";

    public MiembroInscritoEnVotacionException(UUID id) {
        super("El miembro " + id + " tiene candidaturas en votación EN_PROGRESO y no puede ser dado de baja");
    }

    public String getErrorCode() {
        return errorCode;
    }
}
