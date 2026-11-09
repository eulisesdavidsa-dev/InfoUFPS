package org.infoUFPS.Features.Calendar.Commands;

import org.infoUFPS.Enums.EventType;
import org.infoUFPS.ValueObjects.EventSchedule;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Datos de entrada para crear o editar un evento.
 * Los valores se validan en la entidad de dominio y en el objeto de valor de programación.
 */
public record EventRequest(
        String title,
        String type,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime startTime,
        LocalTime endTime,
        String description,
        String infoUrl
) {

    /**
     * @return tipo de evento solicitado o GENERAL si no llega.
     */
    public EventType toEventType() {
        return EventType.fromOrDefault(type);
    }

    /**
     * @return programación validada.
     */
    public EventSchedule toSchedule() {
        return EventSchedule.of(startDate, endDate, startTime, endTime);
    }
}
