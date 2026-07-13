package com.safevoting.elecciones.infrastructure.adapter.in.rest.voto.dto;

import java.time.Instant;
import java.util.UUID;

public record VotoResponse(
        String mensaje,
        UUID votoId,
        Instant fechaEmision
) {}
