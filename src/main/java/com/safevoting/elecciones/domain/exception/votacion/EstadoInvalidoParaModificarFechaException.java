package com.safevoting.elecciones.domain.exception.votacion;

public class EstadoInvalidoParaModificarFechaException extends RuntimeException {
    public EstadoInvalidoParaModificarFechaException(String message) {
        super(message);
    }
}
