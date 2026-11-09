package org.infoUFPS.Features.Calendar.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.CalendarFileDto;
import org.infoUFPS.Entities.Event;
import org.infoUFPS.Enums.Status;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Exceptions.NotFoundException;
import org.infoUFPS.Features.Calendar.Support.EventIcsBuilder;
import org.infoUFPS.Repositories.IEventRepository;
import org.infoUFPS.ValueObjects.EventId;

/**
 * Genera el archivo iCalendar de un evento publicado.
 */
@ApplicationScoped
public class GetEventCalendarQuery {

    private final IEventRepository eventRepository;

    public GetEventCalendarQuery() {
        this.eventRepository = null;
    }

    @Inject
    public GetEventCalendarQuery(IEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Ejecuta la generación del .ics.
     *
     * @param eventId identificador del evento.
     * @return archivo iCalendar.
     */
    public CalendarFileDto execute(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new DomainException("El identificador del evento es obligatorio");
        }
        Event event = eventRepository.findById(EventId.of(eventId))
                .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + eventId));
        if (event.getStatus() != Status.PUBLISHED) {
            throw new NotFoundException("Evento no encontrado: " + eventId);
        }
        return EventIcsBuilder.build(event);
    }
}
