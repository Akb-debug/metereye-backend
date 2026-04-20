// ReleveMapper.java
package com.metereye.backend.mapper;

import com.metereye.backend.dto.ReleveRequestDTO;
import com.metereye.backend.dto.ReleveResponseDTO;
import com.metereye.backend.entity.Releve;
import com.metereye.backend.enums.SourceReleve;
import com.metereye.backend.enums.StatutReleve;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class, SourceReleve.class, StatutReleve.class})
public interface ReleveMapper {

    @Mapping(target = "dateTime", expression = "java(LocalDateTime.now())")
    @Mapping(target = "source", expression = "java(SourceReleve.MANUEL)")
    @Mapping(target = "statut", expression = "java(StatutReleve.VALIDE)")
    @Mapping(target = "consommationCalculee", ignore = true)
    @Mapping(target = "compteur", ignore = true)
    Releve toEntity(ReleveRequestDTO request);

    @Mapping(target = "compteurId", source = "compteur.id")
    @Mapping(target = "compteurReference", source = "compteur.reference")
    ReleveResponseDTO toResponse(Releve releve);

    List<ReleveResponseDTO> toResponseList(List<Releve> releves);
}