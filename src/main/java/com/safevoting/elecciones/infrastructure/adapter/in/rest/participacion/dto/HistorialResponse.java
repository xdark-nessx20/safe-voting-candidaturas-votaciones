package com.safevoting.elecciones.infrastructure.adapter.in.rest.participacion.dto;

import java.time.Instant;
import java.util.UUID;

public record HistorialResponse(
        UUID participacionId,
        String votacionNombre,
        String estado,
        Instant fechaEmision
) {}
