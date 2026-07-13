package com.safevoting.elecciones.application.candidatura;

import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class CancelarCandidaturasPorPartidoUseCase {

    private final CandidaturaRepository candidaturaRepository;

    public Mono<Long> ejecutar(UUID partidoId) {
        return candidaturaRepository.cancelarByPartidoId(partidoId);
    }
}
