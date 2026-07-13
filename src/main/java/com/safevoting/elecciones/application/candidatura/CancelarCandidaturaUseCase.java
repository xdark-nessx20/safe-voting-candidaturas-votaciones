package com.safevoting.elecciones.application.candidatura;

import com.safevoting.elecciones.domain.exception.candidatura.CandidaturaNoEncontradaException;
import com.safevoting.elecciones.domain.exception.votacion.VotacionNoEncontradaException;
import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class CancelarCandidaturaUseCase {

    private final CandidaturaRepository candidaturaRepository;
    private final VotacionRepository votacionRepository;

    public Mono<Candidatura> ejecutar(UUID candidaturaId) {
        return candidaturaRepository.findById(candidaturaId)
                .switchIfEmpty(Mono.error(new CandidaturaNoEncontradaException(candidaturaId)))
                .flatMap(candidatura -> votacionRepository.findById(candidatura.getVotacionId())
                        .switchIfEmpty(Mono.error(new VotacionNoEncontradaException(candidatura.getVotacionId())))
                        .then(Mono.defer(() -> {
                            candidatura.cancelar();
                            return candidaturaRepository.update(candidatura);
                        })));
    }
}
