package com.safevoting.elecciones.infrastructure.adapter.in.rest.voto.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VotoRequest(
        @NotNull UUID candidaturaId
) {}
