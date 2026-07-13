package com.safevoting.elecciones.infrastructure.adapter.in.rest.votacion.dto;

import java.time.Instant;
import java.util.UUID;

public record VotacionResponse(
        UUID id,
        String nombre,
        String tipo,
        String alcance,
        UUID departamentoId,
        UUID municipioId,
        Instant fechaInicio,
        Instant fechaFin,
        String estado,
        String motivo
) {}
