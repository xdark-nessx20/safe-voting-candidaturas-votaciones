package com.safevoting.elecciones.domain.exception.partido;

import java.util.UUID;

public class PartidoConCandidatosEnVotacionException extends RuntimeException {

    private final String errorCode;

    public PartidoConCandidatosEnVotacionException(UUID id) {
        super("No se puede inhabilitar el partido con id " + id + " porque tiene candidatos en votación EN_PROGRESO");
        this.errorCode = "PARTIDO_CON_CANDIDATOS_EN_VOTACION";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
