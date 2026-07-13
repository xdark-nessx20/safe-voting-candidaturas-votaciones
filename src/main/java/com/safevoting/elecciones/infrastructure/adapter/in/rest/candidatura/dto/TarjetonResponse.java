package com.safevoting.elecciones.infrastructure.adapter.in.rest.candidatura.dto;

import java.time.Instant;
import java.util.UUID;

public record TarjetonResponse(
        UUID id,
        UUID miembroPartidoId,
        String nombreCandidato,
        String documentoIdentidad,
        String nombrePartido,
        String logoPartido,
        String fotoUrl,
        UUID votacionId,
        Instant fechaInscripcion,
        String estado
) {}
