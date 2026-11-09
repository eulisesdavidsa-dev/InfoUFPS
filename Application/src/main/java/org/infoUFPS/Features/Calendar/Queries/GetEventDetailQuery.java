package org.infoUFPS.Features.Calendar.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.EventDto;
import org.infoUFPS.Entities.Event;
import org.infoUFPS.Enums.Status;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Exceptions.NotFoundException;
import org.infoUFPS.Features.Calendar.Support.EventDtoAssembler;
import org.infoUFPS.Repositories.IEventRepository;
import org.infoUFPS.ValueObjects.EventId;

/**
 * Consulta el detalle público de un evento publicado.
 */
@ApplicationScoped
public class GetEventDetailQuery {

    private final IEventRepository eventRepository;
    private final EventDtoAssembler assembler;

    public GetEventDetailQuery() {
        this.eventRepository = null;
        this.assembler = null;
    }

    @Inject
    public GetEventDetailQuery(IEventRepository eventRepository, EventDtoAssembler assembler) {
        this.eventRepository = eventRepository;
        this.assembler = assembler;
    }

    /**
     * Obtiene el detalle de un evento publicado.
     *
     * @param eventId identificador del evento.
     * @return detalle del evento.
     */
    public EventDto execute(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new DomainException("El identificador del evento es obligatorio");
        }
        Event event = eventRepository.findById(EventId.of(eventId))
                .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + eventId));
        if (event.getStatus() != Status.PUBLISHED) {
            throw new NotFoundException("Evento no encontrado: " + eventId);
        }
        return assembler.toDto(event);
    }
}
