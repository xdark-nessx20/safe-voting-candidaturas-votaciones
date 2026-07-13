package com.safevoting.elecciones.domain.model.partido;

public enum EstadoMiembro {
    ACTIVO,
    INACTIVO;

    public boolean esActivo() {
        return this == ACTIVO;
    }

    public boolean esInactivo() {
        return this == INACTIVO;
    }
}
