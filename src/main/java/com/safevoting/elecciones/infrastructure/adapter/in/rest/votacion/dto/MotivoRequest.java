package com.safevoting.elecciones.infrastructure.adapter.in.rest.votacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MotivoRequest(
        @NotBlank @Size(min = 10, max = 500) String motivo
) {}
