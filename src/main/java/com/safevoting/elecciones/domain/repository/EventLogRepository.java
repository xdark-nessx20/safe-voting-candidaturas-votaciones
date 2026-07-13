package com.safevoting.elecciones.domain.repository;

import reactor.core.publisher.Mono;

public interface EventLogRepository {

    Mono<Boolean> existsByEventId(String eventId);

    Mono<Void> saveEventId(String eventId);
}
