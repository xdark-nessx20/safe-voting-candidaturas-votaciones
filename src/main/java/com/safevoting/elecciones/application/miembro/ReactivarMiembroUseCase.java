package com.safevoting.elecciones.application.miembro;

import com.safevoting.elecciones.application.miembro.event.UsuarioHabilitadoEvent;
import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.repository.EventLogRepository;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ReactivarMiembroUseCase {

    private final MiembroPartidoRepository miembroRepository;
    private final EventLogRepository eventLogRepository;

    public Mono<Void> ejecutar(UsuarioHabilitadoEvent event) {
        return eventLogRepository.existsByEventId(event.eventId())
                .filter(processed -> !processed)
                .flatMap(__ -> Flux.defer(() -> buscarMiembrosParaReactivar(event.usuarioId()))
                        .flatMap(this::reactivarYActualizar)
                        .then(guardarEvento(event.eventId())));
    }

    private Flux<MiembroPartido> buscarMiembrosParaReactivar(UUID usuarioId) {
        return miembroRepository.findByUsuarioId(usuarioId)
                .filter(this::miembroInactivoConMotivo);
    }

    private boolean miembroInactivoConMotivo(MiembroPartido miembro){
        return miembro.isInactivo()
                && miembro.getMotivoBaja() != null
                && miembro.getMotivoBaja().startsWith("USUARIO_INHABILITADO");
    }

    private Mono<MiembroPartido> reactivarYActualizar(MiembroPartido miembro) {
        miembro.reactivar();
        return miembroRepository.update(miembro);
    }

    private Mono<Void> guardarEvento(String eventId) {
        return eventLogRepository.saveEventId(eventId);
    }
}
