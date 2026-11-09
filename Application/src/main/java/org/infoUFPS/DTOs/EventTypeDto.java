package org.infoUFPS.DTOs;

import org.infoUFPS.Enums.EventType;

/**
 * DTO para exponer los tipos de evento disponibles.
 *
 * @param value valor usado por la API.
 * @param label nombre visible.
 */
public record EventTypeDto(String value, String label) {

    public static EventTypeDto from(EventType type) {
        return new EventTypeDto(type.getValue(), type.getLabel());
    }
}
