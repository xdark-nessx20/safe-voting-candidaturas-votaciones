package com.safevoting.elecciones.application.voto;

import java.time.Instant;
import java.util.UUID;

public record HistorialItem(
        UUID participacionId,
        String votacionNombre,
        String estado,
        Instant fechaEmision
) {}
