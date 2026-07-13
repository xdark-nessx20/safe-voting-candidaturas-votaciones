package com.safevoting.elecciones.domain.repository;

import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MiembroPartidoRepository {

    Mono<MiembroPartido> save(MiembroPartido miembro);

    Mono<MiembroPartido> findById(UUID id);

    Flux<MiembroPartido> findByPartidoId(UUID partidoId);

    Flux<MiembroPartido> findByUsuarioId(UUID usuarioId);

    Mono<Boolean> existsByUsuarioIdAndPartidoId(UUID usuarioId, UUID partidoId);

    Mono<MiembroPartido> update(MiembroPartido miembro);
}
