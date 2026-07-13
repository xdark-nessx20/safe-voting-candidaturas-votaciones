package com.safevoting.elecciones.application.miembro;

import com.safevoting.elecciones.application.miembro.event.UsuarioInhabilitadoEvent;
import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.repository.EventLogRepository;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DesactivarMiembroUseCase {

    private final MiembroPartidoRepository miembroRepository;
    private final EventLogRepository eventLogRepository;

    public Mono<Void> ejecutar(UsuarioInhabilitadoEvent event) {
        return eventLogRepository.existsByEventId(event.eventId())
                .filter(processed -> !processed)
                .flatMap(__ -> Flux.defer(() -> buscarMiembrosActivos(event.usuarioId()))
                        .flatMap(miembro -> desactivarYActualizar(miembro, event.motivo()))
                        .then(guardarEvento(event.eventId())));
    }

    private Flux<MiembroPartido> buscarMiembrosActivos(UUID usuarioId) {
        return miembroRepository.findByUsuarioId(usuarioId)
                .filter(MiembroPartido::isActivo);
    }

    private Mono<MiembroPartido> desactivarYActualizar(MiembroPartido miembro, String motivoEvento) {
        String motivo = "USUARIO_INHABILITADO: " + (motivoEvento != null ? motivoEvento : "");
        miembro.desactivar(motivo);
        return miembroRepository.update(miembro);
    }

    private Mono<Void> guardarEvento(String eventId) {
        return eventLogRepository.saveEventId(eventId);
    }
}
