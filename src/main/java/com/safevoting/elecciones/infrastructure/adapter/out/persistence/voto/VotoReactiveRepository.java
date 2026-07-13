package com.safevoting.elecciones.infrastructure.adapter.out.persistence.voto;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VotoReactiveRepository extends ReactiveCrudRepository<VotoEntity, UUID> {
}
