// CompteurMapper.java
package com.metereye.backend.mapper;

import com.metereye.backend.dto.CompteurRequestDTO;
import com.metereye.backend.dto.CompteurResponseDTO;
import com.metereye.backend.entity.Compteur;
import com.metereye.backend.enums.TypeCompteur;
import org.mapstruct.*;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring", imports = {LocalDate.class, TypeCompteur.class})
public interface CompteurMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "proprietaire", ignore = true)
    @Mapping(target = "releves", ignore = true)
    @Mapping(target = "moduleESP32", ignore = true)
    @Mapping(target = "dateInitialisation", expression = "java(LocalDate.now())")
    @Mapping(target = "actif", constant = "true")
    Compteur toEntity(CompteurRequestDTO request);

    @Mapping(target = "typeCompteur", expression = "java(compteur.getTypeCompteur().name())")
    @Mapping(target = "proprietaireId", source = "proprietaire.id")
    @Mapping(target = "proprietaireNom", expression = "java(compteur.getProprietaire().getNomComplet())")
    @Mapping(target = "valeurActuelle", expression = "java(compteur.getValeurActuelle())")
    CompteurResponseDTO toResponse(Compteur compteur);

    List<CompteurResponseDTO> toResponseList(List<Compteur> compteurs);
}