package com.safevoting.elecciones.domain.model.candidatura;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class Candidatura {

    private UUID id;
    private UUID candidatoId;
    private UUID partidoId;
    private UUID votacionId;
    private String estado;
}
