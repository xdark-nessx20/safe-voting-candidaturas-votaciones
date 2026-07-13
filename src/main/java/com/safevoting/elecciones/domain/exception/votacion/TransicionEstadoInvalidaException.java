package com.safevoting.elecciones.domain.exception.votacion;

import com.safevoting.elecciones.domain.model.votacion.EstadoVotacion;

public class TransicionEstadoInvalidaException extends RuntimeException {
    private final String errorCode = "TRANSICION_ESTADO_INVALIDA";

    public TransicionEstadoInvalidaException(EstadoVotacion actual, EstadoVotacion destino) {
        super("Transición inválida de " + actual + " a " + destino);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
