package com.safevoting.elecciones.infrastructure.adapter.in.rest.miembro.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MiembroRequest(
        @NotNull UUID usuarioId,
        String nombreCompleto,
        String documentoIdentidad,
        String lugarInscripcion,
        String fotoBase64
) {}
