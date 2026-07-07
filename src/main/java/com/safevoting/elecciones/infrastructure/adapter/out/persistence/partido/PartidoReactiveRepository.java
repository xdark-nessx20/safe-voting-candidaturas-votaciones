package com.safevoting.elecciones.infrastructure.adapter.out.persistence.partido;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PartidoReactiveRepository extends ReactiveCrudRepository<PartidoEntity, UUID> {
}
