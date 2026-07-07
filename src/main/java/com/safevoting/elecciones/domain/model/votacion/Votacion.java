package com.safevoting.elecciones.domain.model.votacion;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class Votacion {

    private UUID id;
    private String nombre;
    private EstadoVotacion estado;
}
