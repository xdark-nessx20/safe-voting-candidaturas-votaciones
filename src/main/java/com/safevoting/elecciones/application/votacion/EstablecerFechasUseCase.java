package com.safevoting.elecciones.application.votacion;

import com.safevoting.elecciones.domain.exception.votacion.VotacionNoEncontradaException;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class EstablecerFechasUseCase {

    private final VotacionRepository repository;

    public Mono<Votacion> ejecutar(UUID votacionId, Instant fechaInicio, Instant fechaFin) {
        return repository.findById(votacionId)
                .switchIfEmpty(Mono.error(new VotacionNoEncontradaException(votacionId)))
                .flatMap(votacion -> {
                    votacion.establecerFechas(fechaInicio, fechaFin);
                    return repository.update(votacion);
                });
    }
}
