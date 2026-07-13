package com.safevoting.elecciones.domain.repository;

import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CandidaturaRepository {

    Flux<Candidatura> findActivasByPartidoId(UUID partidoId);

    Mono<Votacion> findVotacionByCandidaturaId(UUID candidaturaId);

    Flux<Candidatura> findActivasByMiembroId(UUID miembroId);
}
