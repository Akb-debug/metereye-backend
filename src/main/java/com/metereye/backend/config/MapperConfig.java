// MapperConfig.java
package com.metereye.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration pour les MapStruct Mappers
 */
@Configuration
public class MapperConfig {

    @Bean
    public com.metereye.backend.mapper.AlerteMapper alerteMapper() {
        return new com.metereye.backend.mapper.AlerteMapperImpl();
    }
}
