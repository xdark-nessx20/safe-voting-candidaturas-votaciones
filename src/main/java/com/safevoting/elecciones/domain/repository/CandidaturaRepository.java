package com.safevoting.elecciones.domain.repository;

import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import com.safevoting.elecciones.application.candidatura.TarjetonItem;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CandidaturaRepository {

    Mono<Candidatura> save(Candidatura candidatura);

    Mono<Candidatura> findById(UUID id);

    Flux<Candidatura> findActivasByVotacionId(UUID votacionId);

    Mono<Long> countActivasByVotacionId(UUID votacionId);

    Flux<Candidatura> findActivasByPartidoId(UUID partidoId);

    Flux<Candidatura> findActivasByMiembroId(UUID miembroPartidoId);

    Mono<Long> cancelarByPartidoId(UUID partidoId);

    Mono<Candidatura> update(Candidatura candidatura);

    Mono<Votacion> findVotacionByCandidaturaId(UUID candidaturaId);

    Flux<TarjetonItem> findTarjetonByVotacionId(UUID votacionId);
}
