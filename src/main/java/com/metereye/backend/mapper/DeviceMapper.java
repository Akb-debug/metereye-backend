// DeviceMapper.java
package com.metereye.backend.mapper;

import com.metereye.backend.dto.DeviceResponseDTO;
import com.metereye.backend.entity.ModuleESP32;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    @Mapping(target = "proprietaireId", expression = "java(module.getProprietaire() != null ? module.getProprietaire().getId() : null)")
    @Mapping(target = "compteurId", expression = "java(module.getCompteur() != null ? module.getCompteur().getId() : null)")
    @Mapping(target = "compteurReference", expression = "java(module.getCompteur() != null ? module.getCompteur().getReference() : null)")
    DeviceResponseDTO toResponse(ModuleESP32 module);
}
