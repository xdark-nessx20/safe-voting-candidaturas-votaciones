package com.safevoting.elecciones.application.candidatura;

import com.safevoting.elecciones.domain.exception.candidatura.MiembroNoActivoException;
import com.safevoting.elecciones.domain.exception.candidatura.PartidoInhabilitadoException;
import com.safevoting.elecciones.domain.exception.candidatura.VotacionNoActivaException;
import com.safevoting.elecciones.domain.exception.miembro.MiembroNoEncontradoException;
import com.safevoting.elecciones.domain.exception.partido.PartidoNoEncontradoException;
import com.safevoting.elecciones.domain.exception.votacion.VotacionNoEncontradaException;
import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class InscribirCandidaturaUseCase {

    private final CandidaturaRepository candidaturaRepository;
    private final VotacionRepository votacionRepository;
    private final MiembroPartidoRepository miembroPartidoRepository;
    private final PartidoPoliticoRepository partidoRepository;

    public Mono<Candidatura> ejecutar(UUID miembroPartidoId, UUID votacionId) {
        return votacionRepository.findById(votacionId)
                .switchIfEmpty(Mono.error(new VotacionNoEncontradaException(votacionId)))
                .filter(Votacion::isActiva)
                .switchIfEmpty(Mono.error(new VotacionNoActivaException(votacionId)))
                .then(Mono.defer(() -> miembroPartidoRepository.findById(miembroPartidoId)))
                .switchIfEmpty(Mono.error(new MiembroNoEncontradoException(miembroPartidoId)))
                .filter(MiembroPartido::isActivo)
                .switchIfEmpty(Mono.error(new MiembroNoActivoException(miembroPartidoId)))
                .flatMap(miembro -> partidoRepository.findById(miembro.getPartidoId())
                        .switchIfEmpty(Mono.error(new PartidoNoEncontradoException(miembro.getPartidoId())))
                        .filter(PartidoPolitico::isHabilitado)
                        .switchIfEmpty(Mono.error(new PartidoInhabilitadoException(miembro.getPartidoId())))
                        .then(Mono.defer(() -> guardarCandidatura(miembroPartidoId, miembro.getPartidoId(), votacionId))));
    }

    private Mono<Candidatura> guardarCandidatura(UUID miembroPartidoId, UUID partidoId, UUID votacionId) {
        Candidatura candidatura = Candidatura.builder()
                .miembroPartidoId(miembroPartidoId)
                .partidoId(partidoId)
                .votacionId(votacionId)
                .build();
        candidatura.validateInfo();
        return candidaturaRepository.save(candidatura);
    }
}
