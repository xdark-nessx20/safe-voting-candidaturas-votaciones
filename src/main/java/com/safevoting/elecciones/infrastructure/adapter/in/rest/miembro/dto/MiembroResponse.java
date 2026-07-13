package com.safevoting.elecciones.infrastructure.adapter.in.rest.miembro.dto;

import java.util.UUID;

public record MiembroResponse(
        UUID id,
        UUID usuarioId,
        String nombreCompleto,
        String documentoIdentidad,
        String lugarInscripcion,
        UUID partidoId,
        String nombrePartido,
        String fotoUrl,
        String estado,
        boolean verificado
) {}
