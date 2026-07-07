package com.safevoting.elecciones.infrastructure.adapter.in.rest.partido.dto;

import java.util.UUID;

public record PartidoResponse(
        UUID id,
        String nombre,
        String descripcion,
        String logoUrl,
        String estado
) {
}
