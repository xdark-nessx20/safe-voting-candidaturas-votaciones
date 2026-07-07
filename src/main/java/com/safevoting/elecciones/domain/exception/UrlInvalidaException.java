package com.safevoting.elecciones.domain.exception;

public class UrlInvalidaException extends RuntimeException {
    public UrlInvalidaException(String message) {
        super(message);
    }
}
