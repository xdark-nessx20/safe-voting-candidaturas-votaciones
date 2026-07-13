package com.safevoting.elecciones.infrastructure.adapter.out.persistence.candidatura;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CandidaturaReactiveRepository extends ReactiveCrudRepository<CandidaturaEntity, UUID> {

    Flux<CandidaturaEntity> findByVotacionIdAndEstado(UUID votacionId, String estado);

    @Query("SELECT COUNT(*) FROM candidaturas WHERE votacion_id = :votacionId AND estado = 'ACTIVA'")
    Mono<Long> countActivasByVotacionId(UUID votacionId);

    Flux<CandidaturaEntity> findByPartidoIdAndEstado(UUID partidoId, String estado);

    Flux<CandidaturaEntity> findByMiembroPartidoIdAndEstado(UUID miembroPartidoId, String estado);
}
