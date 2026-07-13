package com.safevoting.elecciones.infrastructure.adapter.in.rest.votacion.dto;

import com.safevoting.elecciones.domain.model.votacion.TipoVotacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VotacionRequest(
        @NotBlank String nombre,
        @NotNull TipoVotacion tipo,
        UUID departamentoId,
        UUID municipioId
) {}
