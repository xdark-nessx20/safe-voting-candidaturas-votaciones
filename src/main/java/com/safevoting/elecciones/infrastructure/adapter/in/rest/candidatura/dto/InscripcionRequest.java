package com.safevoting.elecciones.infrastructure.adapter.in.rest.candidatura.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InscripcionRequest(
        @NotNull UUID miembroPartidoId,
        @NotNull UUID votacionId
) {}
