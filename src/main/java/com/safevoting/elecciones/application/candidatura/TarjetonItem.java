package com.safevoting.elecciones.application.candidatura;

import java.time.Instant;
import java.util.UUID;

public record TarjetonItem(
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
