package org.infoUFPS.DTOs;

import java.util.List;

public record GlobalSearchResultDto(
        String query,
        List<ModuleItemDto> contenidos,
        List<ProfesorListItemDto> profesores,
        List<MateriaListItemDto> materias
) {
}

