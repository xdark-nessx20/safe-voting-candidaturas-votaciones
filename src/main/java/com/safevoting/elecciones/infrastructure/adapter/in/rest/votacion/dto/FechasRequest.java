package com.safevoting.elecciones.infrastructure.adapter.in.rest.votacion.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record FechasRequest(
        @NotNull Instant fechaInicio,
        @NotNull Instant fechaFin
) {}
