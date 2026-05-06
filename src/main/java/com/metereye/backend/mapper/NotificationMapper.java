package com.metereye.backend.mapper;

import com.metereye.backend.dto.NotificationResponseDTO;
import com.metereye.backend.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "canal", expression = "java(notification.getCanal().name())")
    @Mapping(target = "status", expression = "java(notification.getStatus().name())")
    NotificationResponseDTO toResponse(Notification notification);

    List<NotificationResponseDTO> toResponseList(List<Notification> notifications);
}