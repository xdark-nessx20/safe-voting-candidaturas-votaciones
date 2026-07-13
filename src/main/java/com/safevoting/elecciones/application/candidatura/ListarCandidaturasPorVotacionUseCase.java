package com.safevoting.elecciones.application.candidatura;

import com.safevoting.elecciones.application.candidatura.TarjetonItem;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RequiredArgsConstructor
public class ListarCandidaturasPorVotacionUseCase {

    private final CandidaturaRepository candidaturaRepository;

    public Flux<TarjetonItem> ejecutar(UUID votacionId) {
        return candidaturaRepository.findTarjetonByVotacionId(votacionId);
    }
}
