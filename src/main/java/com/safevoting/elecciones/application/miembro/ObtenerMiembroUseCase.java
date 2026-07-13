package com.safevoting.elecciones.application.miembro;

import com.safevoting.elecciones.domain.exception.miembro.MiembroNoEncontradoException;
import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class ObtenerMiembroUseCase {

    private final MiembroPartidoRepository repository;

    public Mono<MiembroPartido> ejecutar(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new MiembroNoEncontradoException(id)));
    }
}
