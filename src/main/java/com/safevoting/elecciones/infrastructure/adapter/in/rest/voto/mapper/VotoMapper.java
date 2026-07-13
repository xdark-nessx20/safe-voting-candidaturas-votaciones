package com.safevoting.elecciones.infrastructure.adapter.in.rest.voto.mapper;

import com.safevoting.elecciones.domain.model.participacion.Participacion;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.voto.dto.VotoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VotoMapper {

    @Mapping(target = "mensaje", constant = "Voto emitido exitosamente")
    VotoResponse toResponse(Participacion participacion);
}
