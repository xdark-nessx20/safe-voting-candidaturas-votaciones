package com.safevoting.elecciones.infrastructure.adapter.out.persistence.miembro;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MiembroReactiveRepository extends ReactiveCrudRepository<MiembroEntity, UUID> {
}
