package com.safevoting.elecciones.application.miembro.event;

import java.time.Instant;
import java.util.UUID;

public record UsuarioActualizadoEvent(
        String eventId,
        Instant timestamp,
        UUID usuarioId,
        String nombreCompleto,
        String documentoIdentidad,
        String lugarInscripcion
) {}
