package com.safevoting.elecciones.domain.model.voto;

import com.safevoting.elecciones.domain.exception.DatosInvalidosException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voto {

    private UUID id;
    private UUID votacionId;
    private UUID candidaturaId;

    public void validateInfo() {
        if (votacionId == null || candidaturaId == null) {
            throw new DatosInvalidosException("votacionId y candidaturaId son obligatorios");
        }
    }
}
