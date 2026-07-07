package com.safevoting.elecciones.application.partido;

import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;


@RequiredArgsConstructor
public class ListarPartidosUseCase {

    private final PartidoPoliticoRepository repository;

    public Flux<PartidoPolitico> ejecutar() {
        return repository.findAll();
    }
}
