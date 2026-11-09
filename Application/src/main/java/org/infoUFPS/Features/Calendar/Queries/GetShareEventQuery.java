package org.infoUFPS.Features.Calendar.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.ShareDto;
import org.infoUFPS.Entities.Event;
import org.infoUFPS.Enums.Status;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Exceptions.NotFoundException;
import org.infoUFPS.Features.Calendar.Support.EventShareSupport;
import org.infoUFPS.Features.Calendar.Support.GoogleCalendarLinkBuilder;
import org.infoUFPS.Interfaces.CalendarSettings;
import org.infoUFPS.Repositories.IEventRepository;
import org.infoUFPS.ValueObjects.EventId;

/**
 * Obtiene los datos para compartir un evento publicado.
 */
@ApplicationScoped
public class GetShareEventQuery {

    private final IEventRepository eventRepository;
    private final CalendarSettings settings;

    public GetShareEventQuery() {
        this.eventRepository = null;
        this.settings = null;
    }

    @Inject
    public GetShareEventQuery(IEventRepository eventRepository, CalendarSettings settings) {
        this.eventRepository = eventRepository;
        this.settings = settings;
    }

    /**
     * Ejecuta la consulta.
     *
     * @param eventId identificador del evento.
     * @return texto y enlaces para compartir.
     */
    public ShareDto execute(String eventId) {
        Event event = loadPublished(eventId);
        return new ShareDto(
                EventShareSupport.message(event, settings),
                EventShareSupport.eventUrl(event, settings),
                GoogleCalendarLinkBuilder.build(event),
                EventShareSupport.icsUrl(event, settings)
        );
    }

    private Event loadPublished(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new DomainException("El identificador del evento es obligatorio");
        }
        Event event = eventRepository.findById(EventId.of(eventId))
                .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + eventId));
        if (event.getStatus() != Status.PUBLISHED) {
            throw new NotFoundException("Evento no encontrado: " + eventId);
        }
        return event;
    }
}
