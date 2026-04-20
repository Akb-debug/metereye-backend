// UserMapper.java
package com.metereye.backend.mapper;

import com.metereye.backend.dto.UserProfileDTO;
import com.metereye.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole().getName().name())")
    @Mapping(target = "nomComplet", expression = "java(user.getNomComplet())")
    UserProfileDTO toProfileDTO(User user);
}