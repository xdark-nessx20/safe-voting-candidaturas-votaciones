package com.safevoting.elecciones.domain.exception.votacion;

import com.safevoting.elecciones.domain.model.votacion.AlcanceVotacion;

public class AlcanceExcedidoException extends RuntimeException {
    private final String errorCode = "ALCANCE_EXCEDIDO";

    public AlcanceExcedidoException(AlcanceVotacion alcanceGestor, AlcanceVotacion alcanceVotacion) {
        super("El alcance del gestor (" + alcanceGestor + ") no cubre el alcance de la votación (" + alcanceVotacion + ")");
    }

    public String getErrorCode() {
        return errorCode;
    }
}
