package com.safevoting.elecciones.infrastructure.adapter.out.http.dto;

import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nombreCompleto,
        String documentoIdentidad,
        String lugarInscripcion,
        String estado
) {}
