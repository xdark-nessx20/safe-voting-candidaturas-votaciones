package com.safevoting.elecciones.infrastructure.adapter.out.persistence.candidatura;

import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CandidaturaPersistenceMapper {

    @Mapping(target = "createdAt", ignore = true)
    CandidaturaEntity toEntity(Candidatura domain);

    Candidatura toDomain(CandidaturaEntity entity);
}
