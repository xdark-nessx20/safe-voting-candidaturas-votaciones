package com.safevoting.elecciones.application.votacion;

import com.safevoting.elecciones.domain.exception.votacion.NombreDuplicadoException;
import com.safevoting.elecciones.domain.model.votacion.AlcanceVotacion;
import com.safevoting.elecciones.domain.model.votacion.TipoVotacion;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class CrearVotacionUseCase {

    private final VotacionRepository repository;

    public Mono<Votacion> ejecutar(Votacion votacion) {
        return repository.existsByNombre(votacion.getNombre())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new NombreDuplicadoException(votacion.getNombre())))
                .then(Mono.defer(() -> {
                    votacion.validateInfo();
                    return repository.save(votacion);
                }));
    }
}
