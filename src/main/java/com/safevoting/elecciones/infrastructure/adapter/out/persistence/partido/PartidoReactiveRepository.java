package com.safevoting.elecciones.infrastructure.adapter.out.persistence.partido;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface PartidoReactiveRepository extends ReactiveCrudRepository<PartidoEntity, UUID> {

    Mono<Boolean> existsByNombre(String nombre);
}
