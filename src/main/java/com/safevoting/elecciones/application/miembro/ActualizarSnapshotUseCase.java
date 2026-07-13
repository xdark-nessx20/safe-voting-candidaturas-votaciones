package com.safevoting.elecciones.application.miembro;

import com.safevoting.elecciones.application.miembro.event.UsuarioActualizadoEvent;
import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import com.safevoting.elecciones.domain.repository.EventLogRepository;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ActualizarSnapshotUseCase {

    private static final Logger log = LoggerFactory.getLogger(ActualizarSnapshotUseCase.class);

    private final MiembroPartidoRepository miembroRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final EventLogRepository eventLogRepository;

    public Mono<Void> ejecutar(UsuarioActualizadoEvent event) {
        return eventLogRepository.existsByEventId(event.eventId())
                .filter(processed -> !processed)
                .flatMap(__ -> Flux.defer(() -> miembroRepository.findByUsuarioId(event.usuarioId()))
                        .flatMap(miembro -> actualizarSiProcede(miembro, event))
                        .then(guardarEvento(event.eventId())));
    }

    private Mono<MiembroPartido> actualizarSiProcede(MiembroPartido miembro, UsuarioActualizadoEvent event) {
        return tieneCandidaturaEnProgreso(miembro.getId())
                .filter(enProgreso -> !enProgreso)
                .flatMap(__ -> ejecutarActualizacion(miembro, event))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Snapshot no actualizado para miembro {}: tiene candidaturas en votación EN_PROGRESO", miembro.getId());
                    return Mono.empty();
                }));
    }

    private Mono<Boolean> tieneCandidaturaEnProgreso(UUID miembroId) {
        return candidaturaRepository.findActivasByMiembroId(miembroId)
                .flatMap(c -> candidaturaRepository.findVotacionByCandidaturaId(c.getId()))
                .filter(Votacion::isEnProgreso)
                .hasElements();
    }

    private Mono<MiembroPartido> ejecutarActualizacion(MiembroPartido miembro, UsuarioActualizadoEvent event) {
        miembro.actualizarSnapshot(event.nombreCompleto(), event.documentoIdentidad(), event.lugarInscripcion());
        return miembroRepository.update(miembro);
    }

    private Mono<Void> guardarEvento(String eventId) {
        return eventLogRepository.saveEventId(eventId);
    }
}
