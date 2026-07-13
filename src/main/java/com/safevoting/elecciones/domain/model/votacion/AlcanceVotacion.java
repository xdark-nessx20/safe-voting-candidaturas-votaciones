package com.safevoting.elecciones.domain.model.votacion;

public enum AlcanceVotacion {
    MUNICIPAL,
    DEPARTAMENTAL,
    REGIONAL,
    NACIONAL;

    public boolean cubre(AlcanceVotacion otro) {
        return this.ordinal() >= otro.ordinal();
    }
}
