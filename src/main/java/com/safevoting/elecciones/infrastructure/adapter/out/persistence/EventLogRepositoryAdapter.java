package com.safevoting.elecciones.infrastructure.adapter.out.persistence;

import com.safevoting.elecciones.domain.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class EventLogRepositoryAdapter implements EventLogRepository {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Boolean> existsByEventId(String eventId) {
        return databaseClient.sql("SELECT EXISTS(SELECT 1 FROM event_log WHERE event_id = :eventId)")
                .bind("eventId", eventId)
                .mapValue(Boolean.class)
                .one()
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Void> saveEventId(String eventId) {
        return databaseClient.sql("INSERT INTO event_log (event_id) VALUES (:eventId) ON CONFLICT DO NOTHING")
                .bind("eventId", eventId)
                .then();
    }
}
