package com.safevoting.elecciones.domain.model.partido;

public enum EstadoPartido {
    HABILITADO,
    INHABILITADO;

    public boolean esHabilitado() {
        return this == HABILITADO;
    }

    public boolean esInhabilitado(){
        return this == INHABILITADO;
    }
}
