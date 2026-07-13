package com.safevoting.elecciones.infrastructure.adapter.out.persistence.participacion;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ParticipacionReactiveRepository extends ReactiveCrudRepository<ParticipacionEntity, UUID> {

    Flux<ParticipacionEntity> findByUsuarioId(UUID usuarioId);

    @Query("SELECT EXISTS(SELECT 1 FROM participaciones WHERE usuario_id = :usuarioId AND votacion_id = :votacionId)")
    Mono<Boolean> existsByUsuarioIdAndVotacionId(UUID usuarioId, UUID votacionId);
}
