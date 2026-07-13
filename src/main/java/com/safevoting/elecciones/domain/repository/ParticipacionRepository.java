package com.safevoting.elecciones.domain.repository;

import com.safevoting.elecciones.domain.model.participacion.Participacion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ParticipacionRepository {

    Mono<Participacion> save(Participacion participacion);

    Flux<Participacion> findByUsuarioId(UUID usuarioId);

    Mono<Boolean> existsByUsuarioIdAndVotacionId(UUID usuarioId, UUID votacionId);
}
