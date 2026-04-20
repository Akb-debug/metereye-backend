// AlerteMapper.java
package com.metereye.backend.mapper;

import com.metereye.backend.dto.AlerteResponseDTO;
import com.metereye.backend.entity.Alerte;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AlerteMapper {

    @Mapping(target = "compteurId", source = "compteur.id")
    @Mapping(target = "compteurReference", source = "compteur.reference")
    @Mapping(target = "typeAlerte", expression = "java(alerte.getTypeAlerte().name())")
    AlerteResponseDTO toResponse(Alerte alerte);

    List<AlerteResponseDTO> toResponseList(List<Alerte> alertes);
}