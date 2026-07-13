package com.safevoting.elecciones.application.voto;

import com.safevoting.elecciones.domain.exception.participacion.CandidaturaNoActivaException;
import com.safevoting.elecciones.domain.exception.participacion.UsuarioYaVotoException;
import com.safevoting.elecciones.domain.exception.voto.VotacionNoEnProgresoException;
import com.safevoting.elecciones.domain.exception.votacion.VotacionNoEncontradaException;
import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import com.safevoting.elecciones.domain.model.participacion.Participacion;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.model.voto.Voto;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import com.safevoting.elecciones.domain.repository.ParticipacionRepository;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import com.safevoting.elecciones.domain.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class EmitirVotoUseCase {

    private final VotoRepository votoRepository;
    private final ParticipacionRepository participacionRepository;
    private final VotacionRepository votacionRepository;
    private final CandidaturaRepository candidaturaRepository;

    public Mono<Participacion> ejecutar(UUID usuarioId, UUID votacionId, UUID candidaturaId) {
        return votacionRepository.findById(votacionId)
                .switchIfEmpty(Mono.error(new VotacionNoEncontradaException(votacionId)))
                .filter(Votacion::isEnProgreso)
                .switchIfEmpty(Mono.error(new VotacionNoEnProgresoException(votacionId)))
                .then(Mono.defer(() -> validarCandidatura(votacionId, candidaturaId)))
                .then(Mono.defer(() -> validarNoVotoPrevio(usuarioId, votacionId)))
                .then(Mono.defer(() -> crearVotoYParticipacion(usuarioId, votacionId, candidaturaId)));
    }

    private Mono<Void> validarCandidatura(UUID votacionId, UUID candidaturaId) {
        return candidaturaRepository.findByVotacionIdAndCandidaturaId(votacionId, candidaturaId)
                .filter(Candidatura::isActiva)
                .switchIfEmpty(Mono.error(new CandidaturaNoActivaException(candidaturaId)))
                .then();
    }

    private Mono<Void> validarNoVotoPrevio(UUID usuarioId, UUID votacionId) {
        return participacionRepository.existsByUsuarioIdAndVotacionId(usuarioId, votacionId)
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new UsuarioYaVotoException(usuarioId, votacionId)))
                .then();
    }

    private Mono<Participacion> crearVotoYParticipacion(UUID usuarioId, UUID votacionId, UUID candidaturaId) {
        Voto voto = buildVoto(votacionId, candidaturaId);
        return votoRepository.save(voto)
                .flatMap(votoGuardado -> {
                    Participacion participacion = buildParticipacion(usuarioId, votoGuardado.getId(), votacionId);
                    return participacionRepository.save(participacion);
                });
    }

    private Voto buildVoto(UUID votacionId, UUID candidaturaId) {
        Voto voto = Voto.builder()
                .votacionId(votacionId)
                .candidaturaId(candidaturaId)
                .build();
        voto.validateInfo();
        return voto;
    }

    private Participacion buildParticipacion(UUID usuarioId, UUID votoId, UUID votacionId) {
        Participacion participacion = Participacion.builder()
                .usuarioId(usuarioId)
                .votoId(votoId)
                .votacionId(votacionId)
                .build();
        participacion.validateInfo();
        return participacion;
    }
}
