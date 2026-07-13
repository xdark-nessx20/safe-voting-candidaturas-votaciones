package com.safevoting.elecciones.domain.model.votacion;

public enum TipoVotacion {
    PRESIDENCIA(AlcanceVotacion.NACIONAL),
    CONGRESO(AlcanceVotacion.NACIONAL),
    ALCALDIA(AlcanceVotacion.MUNICIPAL),
    GOBERNACION(AlcanceVotacion.DEPARTAMENTAL);

    private final AlcanceVotacion alcance;

    TipoVotacion(AlcanceVotacion alcance) {
        this.alcance = alcance;
    }

    public AlcanceVotacion alcanceCompatible() {
        return alcance;
    }
}
