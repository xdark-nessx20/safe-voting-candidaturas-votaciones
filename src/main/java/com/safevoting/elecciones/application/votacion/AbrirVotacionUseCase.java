package com.safevoting.elecciones.application.votacion;

import com.safevoting.elecciones.domain.exception.votacion.VotacionNoEncontradaException;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class AbrirVotacionUseCase {

    private final VotacionRepository repository;
    private final CandidaturaRepository candidaturaRepository;

    public Mono<Votacion> ejecutar(UUID votacionId) {
        return repository.findById(votacionId)
                .switchIfEmpty(Mono.error(new VotacionNoEncontradaException(votacionId)))
                .flatMap(votacion -> candidaturaRepository.countActivasByVotacionId(votacionId)
                        .flatMap(count -> {
                            votacion.abrir(count > 0);
                            return repository.update(votacion);
                        }));
    }
}
