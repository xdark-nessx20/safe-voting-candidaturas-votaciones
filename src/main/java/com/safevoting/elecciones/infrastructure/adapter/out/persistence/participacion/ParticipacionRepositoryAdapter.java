package com.safevoting.elecciones.infrastructure.adapter.out.persistence.participacion;

import com.safevoting.elecciones.domain.model.participacion.Participacion;
import com.safevoting.elecciones.domain.repository.ParticipacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ParticipacionRepositoryAdapter implements ParticipacionRepository {

    private final ParticipacionReactiveRepository repository;

    @Override
    public Mono<Participacion> save(Participacion participacion) {
        ParticipacionEntity entity = ParticipacionEntity.builder()
                .usuarioId(participacion.getUsuarioId())
                .votoId(participacion.getVotoId())
                .votacionId(participacion.getVotacionId())
                .estado(participacion.getEstado().name())
                .fechaEmision(participacion.getFechaEmision())
                .build();
        return repository.save(entity)
                .map(this::toDomain);
    }

    @Override
    public Flux<Participacion> findByUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioId(usuarioId).map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByUsuarioIdAndVotacionId(UUID usuarioId, UUID votacionId) {
        return repository.existsByUsuarioIdAndVotacionId(usuarioId, votacionId);
    }

    private Participacion toDomain(ParticipacionEntity e) {
        return Participacion.builder()
                .id(e.getId())
                .usuarioId(e.getUsuarioId())
                .votoId(e.getVotoId())
                .votacionId(e.getVotacionId())
                .estado(com.safevoting.elecciones.domain.model.participacion.EstadoParticipacion.valueOf(e.getEstado()))
                .fechaEmision(e.getFechaEmision())
                .build();
    }
}
