package com.safevoting.elecciones.application.partido;

import com.safevoting.elecciones.domain.exception.partido.PartidoNoEncontradoException;
import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class ObtenerPartidoUseCase {

    private final PartidoPoliticoRepository repository;

    public Mono<PartidoPolitico> ejecutar(UUID partidoId) {
        return repository.findById(partidoId)
                .switchIfEmpty(Mono.error(new PartidoNoEncontradoException(partidoId)));
    }
}
