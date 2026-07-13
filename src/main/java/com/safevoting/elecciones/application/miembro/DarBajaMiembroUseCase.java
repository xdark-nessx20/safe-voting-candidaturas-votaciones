package com.safevoting.elecciones.application.miembro;

import com.safevoting.elecciones.domain.exception.miembro.MiembroInscritoEnVotacionException;
import com.safevoting.elecciones.domain.exception.miembro.MiembroNoEncontradoException;
import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class DarBajaMiembroUseCase {

    private final MiembroPartidoRepository miembroRepository;
    private final CandidaturaRepository candidaturaRepository;

    public Mono<MiembroPartido> ejecutar(UUID miembroId, String motivo) {
        return buscarMiembro(miembroId)
                .flatMap(miembro -> validarSinCandidaturaEnProgreso(miembroId)
                        .then(Mono.defer(() -> ejecutarDesactivacion(miembro, motivo))));
    }

    private Mono<MiembroPartido> buscarMiembro(UUID id) {
        return miembroRepository.findById(id)
                .switchIfEmpty(Mono.error(new MiembroNoEncontradoException(id)));
    }

    private Mono<Void> validarSinCandidaturaEnProgreso(UUID miembroId) {
        return candidaturaRepository.findActivasByMiembroId(miembroId)
                .flatMap(c -> candidaturaRepository.findVotacionByCandidaturaId(c.getId()))
                .filter(Votacion::isEnProgreso)
                .hasElements()
                .filter(enProgreso -> !enProgreso)
                .switchIfEmpty(Mono.error(new MiembroInscritoEnVotacionException(miembroId)))
                .then();
    }

    private Mono<MiembroPartido> ejecutarDesactivacion(MiembroPartido miembro, String motivo) {
        miembro.desactivar(motivo);
        return miembroRepository.update(miembro);
    }
}
