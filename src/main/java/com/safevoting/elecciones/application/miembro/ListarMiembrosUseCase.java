package com.safevoting.elecciones.application.miembro;

import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RequiredArgsConstructor
public class ListarMiembrosUseCase {

    private final MiembroPartidoRepository repository;

    public Flux<MiembroPartido> ejecutar(UUID partidoId) {
        return repository.findByPartidoId(partidoId);
    }
}
