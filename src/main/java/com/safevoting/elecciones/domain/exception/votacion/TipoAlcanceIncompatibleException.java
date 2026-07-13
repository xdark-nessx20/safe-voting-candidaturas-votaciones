package com.safevoting.elecciones.domain.exception.votacion;

import com.safevoting.elecciones.domain.model.votacion.AlcanceVotacion;
import com.safevoting.elecciones.domain.model.votacion.TipoVotacion;

public class TipoAlcanceIncompatibleException extends RuntimeException {
    private final String errorCode = "TIPO_ALCANCE_INCOMPATIBLE";

    public TipoAlcanceIncompatibleException(TipoVotacion tipo, AlcanceVotacion alcance) {
        super("El tipo " + tipo + " no es compatible con el alcance " + alcance);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
