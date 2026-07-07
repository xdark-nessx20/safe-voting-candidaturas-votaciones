package com.safevoting.elecciones.application.partido;

import com.safevoting.elecciones.domain.exception.partido.PartidoConCandidatosEnVotacionException;
import com.safevoting.elecciones.domain.exception.partido.PartidoNoEncontradoException;
import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.domain.model.votacion.EstadoVotacion;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class InhabilitarPartidoUseCase {

    private final PartidoPoliticoRepository partidoRepository;
    private final CandidaturaRepository candidaturaRepository;

    public Mono<PartidoPolitico> ejecutar(UUID partidoId) {
        return partidoRepository.findById(partidoId)
                .switchIfEmpty(Mono.error(new PartidoNoEncontradoException(partidoId)))
                .flatMap(this::validarCandidatosEnVotacion)
                .flatMap(partido -> {
                    partido.inhabilitar();
                    return partidoRepository.update(partido);
                });
    }

    private Mono<PartidoPolitico> validarCandidatosEnVotacion(PartidoPolitico partido) {
        return candidaturaRepository.findActivasByPartidoId(partido.getId())
                .flatMap(candidatura ->
                        candidaturaRepository.findVotacionByCandidaturaId(candidatura.getId())
                )
                .any(votacion -> votacion.getEstado() == EstadoVotacion.EN_PROGRESO)
                .filter(tieneEnProgreso -> !tieneEnProgreso)
                .switchIfEmpty(Mono.error(new PartidoConCandidatosEnVotacionException(partido.getId())))
                .map(ignored -> partido);
    }
}
