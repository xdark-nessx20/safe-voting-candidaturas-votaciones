package com.safevoting.elecciones.application.voto;

import com.safevoting.elecciones.domain.repository.ParticipacionRepository;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RequiredArgsConstructor
public class ConsultarHistorialUseCase {

    private final ParticipacionRepository participacionRepository;
    private final VotacionRepository votacionRepository;

    public Flux<HistorialItem> ejecutar(UUID usuarioId) {
        return participacionRepository.findByUsuarioId(usuarioId)
                .flatMap(p -> votacionRepository.findById(p.getVotacionId())
                        .map(v -> new HistorialItem(
                                p.getId(),
                                v.getNombre(),
                                p.getEstado().name(),
                                p.getFechaEmision()
                        )));
    }
}
