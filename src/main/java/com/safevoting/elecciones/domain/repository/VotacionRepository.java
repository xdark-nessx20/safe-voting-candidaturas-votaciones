package com.safevoting.elecciones.domain.repository;

import com.safevoting.elecciones.domain.model.votacion.Votacion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface VotacionRepository {

    Mono<Votacion> save(Votacion votacion);

    Mono<Votacion> findById(UUID id);

    Flux<Votacion> findAll();

    Mono<Boolean> existsByNombre(String nombre);

    Mono<Votacion> update(Votacion votacion);

    Flux<Votacion> findActivasConFechaInicioVencida(Instant ahora);

    Flux<Votacion> findEnProgresoConFechaFinVencida(Instant ahora);
}
