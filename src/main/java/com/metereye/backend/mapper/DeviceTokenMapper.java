package com.metereye.backend.mapper;

import com.metereye.backend.dto.DeviceTokenRequestDTO;
import com.metereye.backend.entity.DeviceToken;
import com.metereye.backend.enums.DevicePlatform;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeviceTokenMapper {

    @Mapping(target = "platform", source = "platform")
    @Mapping(target = "actif", constant = "true")
    DeviceToken toEntity(DeviceTokenRequestDTO dto);

    default DevicePlatform map(String value) {
        return DevicePlatform.valueOf(value);
    }
}