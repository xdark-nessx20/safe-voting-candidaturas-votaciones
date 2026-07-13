package com.safevoting.elecciones.application.votacion;

import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class ListarVotacionesUseCase {

    private final VotacionRepository repository;

    public Flux<Votacion> ejecutar() {
        return repository.findAll();
    }
}
