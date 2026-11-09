package org.infoUFPS.Features.Catalogos.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import org.infoUFPS.DTOs.CatalogoItemDto;
import org.infoUFPS.Enums.Facultad;
import org.infoUFPS.Enums.TipoMateria;

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class GetCatalogosQuery {

    public List<CatalogoItemDto> getFacultades() {
        return Arrays.stream(Facultad.values())
                .map(f -> CatalogoItemDto.of(f.name(), f.getDisplayName(), "Facultad de " + f.getDisplayName()))
                .toList();
    }

    public List<CatalogoItemDto> getTiposMateria() {
        return Arrays.stream(TipoMateria.values())
                .map(t -> CatalogoItemDto.of(t.name(), t.getDisplayName(), "Tipo de materia: " + t.getDisplayName()))
                .toList();
    }

    public List<Integer> getCreditos() {
        return List.of(1, 2, 3, 4, 5, 6, 8, 10);
    }
}

