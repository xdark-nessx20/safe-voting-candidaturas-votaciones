package com.safevoting.elecciones.application.votacion;

import com.safevoting.elecciones.domain.exception.votacion.VotacionNoEncontradaException;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class CompletarVotacionUseCase {

    private final VotacionRepository repository;

    public Mono<Votacion> ejecutar(UUID votacionId) {
        return repository.findById(votacionId)
                .switchIfEmpty(Mono.error(new VotacionNoEncontradaException(votacionId)))
                .flatMap(votacion -> {
                    votacion.completar();
                    return repository.update(votacion);
                });
    }
}
