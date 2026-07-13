package com.safevoting.elecciones.domain.exception.participacion;

public class ParticipacionYaAnuladaException extends RuntimeException {
    public ParticipacionYaAnuladaException(String message) {
        super(message);
    }
}
