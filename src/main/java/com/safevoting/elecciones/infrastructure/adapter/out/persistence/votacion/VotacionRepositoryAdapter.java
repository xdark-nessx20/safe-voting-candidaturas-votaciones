package com.safevoting.elecciones.infrastructure.adapter.out.persistence.votacion;

import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VotacionRepositoryAdapter implements VotacionRepository {

    private final VotacionReactiveRepository repository;
    private final DatabaseClient databaseClient;
    private final VotacionPersistenceMapper mapper;

    @Override
    public Mono<Votacion> save(Votacion votacion) {
        return repository.save(mapper.toEntity(votacion)).map(mapper::toDomain);
    }

    @Override
    public Mono<Votacion> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<Votacion> findAll() {
        return repository.findAll().map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNombre(String nombre) {
        return repository.existsByNombre(nombre);
    }

    @Override
    public Mono<Votacion> update(Votacion votacion) {
        return databaseClient.sql("UPDATE votaciones SET fecha_inicio = :fechaInicio, fecha_fin = :fechaFin, estado = :estado, motivo = :motivo, updated_at = NOW() WHERE id = :id")
                .bind("id", votacion.getId())
                .bind("fechaInicio", votacion.getFechaInicio())
                .bind("fechaFin", votacion.getFechaFin())
                .bind("estado", votacion.getEstado().name())
                .bind("motivo", votacion.getMotivo())
                .fetch()
                .rowsUpdated()
                .thenReturn(votacion);
    }

    @Override
    public Flux<Votacion> findActivasConFechaInicioVencida(Instant ahora) {
        return repository.findActivasConFechaInicioVencida(ahora).map(mapper::toDomain);
    }

    @Override
    public Flux<Votacion> findEnProgresoConFechaFinVencida(Instant ahora) {
        return repository.findEnProgresoConFechaFinVencida(ahora).map(mapper::toDomain);
    }
}
