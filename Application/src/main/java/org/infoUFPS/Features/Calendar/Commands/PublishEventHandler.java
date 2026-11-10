package org.infoUFPS.Features.Calendar.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.EventDto;
import org.infoUFPS.Entities.Event;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Exceptions.NotFoundException;
import org.infoUFPS.Features.Calendar.Support.EventDuplicateGuard;
import org.infoUFPS.Features.Calendar.Support.EventDtoAssembler;
import org.infoUFPS.Repositories.IEventRepository;
import org.infoUFPS.ValueObjects.EventId;

/**
 * Caso de uso para publicar un evento en revisión.
 */
@ApplicationScoped
public class PublishEventHandler {

    private final IEventRepository eventRepository;
    private final EventDtoAssembler assembler;
    private final EventDuplicateGuard duplicateGuard;

    public PublishEventHandler() {
        this.eventRepository = null;
        this.assembler = null;
        this.duplicateGuard = null;
    }

    @Inject
    public PublishEventHandler(IEventRepository eventRepository,
                               EventDtoAssembler assembler,
                               EventDuplicateGuard duplicateGuard) {
        this.eventRepository = eventRepository;
        this.assembler = assembler;
        this.duplicateGuard = duplicateGuard;
    }

    /**
     * Publica el evento tras comprobar que no exista otro evento publicado con
     * el mismo título, fecha y hora.
     *
     * @param command identificador del evento.
     * @return evento publicado.
     */
    public EventDto execute(PublishEventCommand command) {
        if (command == null || command.eventId() == null || command.eventId().isBlank()) {
            throw new DomainException("El identificador del evento es obligatorio");
        }
        Event event = eventRepository.findById(EventId.of(command.eventId()))
                .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + command.eventId()));

        duplicateGuard.ensureNoPublishedDuplicate(
                event.getTitle(),
                event.getStartDate(),
                event.getStartTime(),
                event.getId());

        event.publish();
        eventRepository.save(event);
        return assembler.toDto(event);
    }
}
