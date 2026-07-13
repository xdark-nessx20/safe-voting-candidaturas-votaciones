package com.safevoting.elecciones.infrastructure.adapter.out.persistence.voto;

import com.safevoting.elecciones.domain.model.voto.Voto;
import com.safevoting.elecciones.domain.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class VotoRepositoryAdapter implements VotoRepository {

    private final VotoReactiveRepository repository;

    @Override
    public Mono<Voto> save(Voto voto) {
        VotoEntity entity = VotoEntity.builder()
                .votacionId(voto.getVotacionId())
                .candidaturaId(voto.getCandidaturaId())
                .build();
        return repository.save(entity)
                .map(e -> Voto.builder()
                        .id(e.getId())
                        .votacionId(e.getVotacionId())
                        .candidaturaId(e.getCandidaturaId())
                        .build());
    }
}
