package com.safevoting.elecciones.application.miembro.event;

import java.time.Instant;
import java.util.UUID;

public record UsuarioInhabilitadoEvent(
        String eventId,
        Instant timestamp,
        UUID usuarioId,
        String motivo
) {}
