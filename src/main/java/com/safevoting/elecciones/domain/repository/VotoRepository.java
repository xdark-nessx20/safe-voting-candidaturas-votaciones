package com.safevoting.elecciones.domain.repository;

import com.safevoting.elecciones.domain.model.voto.Voto;
import reactor.core.publisher.Mono;

public interface VotoRepository {

    Mono<Voto> save(Voto voto);
}
