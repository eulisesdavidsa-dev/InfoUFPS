package org.infoUFPS.Features.Calendar.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import org.infoUFPS.DTOs.EventTypeDto;
import org.infoUFPS.Enums.EventType;

import java.util.Arrays;
import java.util.List;

/**
 * Consulta el catálogo de tipos de evento con su nombre visible.
 */
@ApplicationScoped
public class GetEventTypesQuery {

    /**
     * @return tipos disponibles.
     */
    public List<EventTypeDto> execute() {
        return Arrays.stream(EventType.values())
                .map(EventTypeDto::from)
                .toList();
    }
}
