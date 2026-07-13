package com.safevoting.elecciones.infrastructure.adapter.out.persistence.votacion;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface VotacionReactiveRepository extends ReactiveCrudRepository<VotacionEntity, UUID> {

    Mono<Boolean> existsByNombre(String nombre);

    @Query("SELECT * FROM votaciones WHERE estado = 'ACTIVA' AND fecha_inicio IS NOT NULL AND fecha_inicio <= :ahora")
    Flux<VotacionEntity> findActivasConFechaInicioVencida(Instant ahora);

    @Query("SELECT * FROM votaciones WHERE estado = 'EN_PROGRESO' AND fecha_fin IS NOT NULL AND fecha_fin <= :ahora")
    Flux<VotacionEntity> findEnProgresoConFechaFinVencida(Instant ahora);
}
