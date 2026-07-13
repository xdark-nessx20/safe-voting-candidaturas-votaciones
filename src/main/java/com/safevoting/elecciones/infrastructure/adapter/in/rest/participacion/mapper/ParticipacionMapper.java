package com.safevoting.elecciones.infrastructure.adapter.in.rest.participacion.mapper;

import com.safevoting.elecciones.application.voto.HistorialItem;
import com.safevoting.elecciones.infrastructure.adapter.in.rest.participacion.dto.HistorialResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParticipacionMapper {

    HistorialResponse toResponse(HistorialItem item);
}
