package org.infoUFPS.DTOs;

import java.util.List;

public record GlobalSearchResultDto(
        String query,
        List<ModuleItemDto> contenidos,
        List<PublicProfesorListItemDto> profesores,
        List<PublicMateriaListItemDto> materias
) {
}
