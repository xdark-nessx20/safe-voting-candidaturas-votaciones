package com.safevoting.elecciones.domain.repository;

import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PartidoPoliticoRepository {

    Mono<PartidoPolitico> save(PartidoPolitico partido);

    Mono<PartidoPolitico> findById(UUID id);

    Flux<PartidoPolitico> findAll();

    Mono<Boolean> existsByNombre(String nombre);

    Mono<PartidoPolitico> update(PartidoPolitico partido);
}
