package com.safevoting.elecciones.infrastructure.adapter.out.persistence.miembro;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface MiembroReactiveRepository extends ReactiveCrudRepository<MiembroEntity, UUID> {

    Flux<MiembroEntity> findByPartidoId(UUID partidoId);

    Flux<MiembroEntity> findByUsuarioId(UUID usuarioId);

    @Query("SELECT EXISTS(SELECT 1 FROM miembros_partidos WHERE usuario_id = :usuarioId AND partido_id = :partidoId)")
    Mono<Boolean> existsByUsuarioIdAndPartidoId(UUID usuarioId, UUID partidoId);
}
