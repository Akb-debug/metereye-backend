package com.metereye.backend.mapper;

import com.metereye.backend.dto.NotificationPreferenceRequestDTO;
import com.metereye.backend.dto.NotificationPreferenceResponseDTO;
import com.metereye.backend.entity.NotificationPreference;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationPreferenceMapper {

    NotificationPreferenceResponseDTO toResponse(NotificationPreference entity);

    NotificationPreference toEntity(NotificationPreferenceRequestDTO dto);
}