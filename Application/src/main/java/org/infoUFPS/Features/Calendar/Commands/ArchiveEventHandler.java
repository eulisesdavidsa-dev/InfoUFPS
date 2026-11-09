package org.infoUFPS.Features.Calendar.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.EventDto;
import org.infoUFPS.Entities.Event;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Exceptions.NotFoundException;
import org.infoUFPS.Features.Calendar.Support.EventDtoAssembler;
import org.infoUFPS.Repositories.IEventRepository;
import org.infoUFPS.ValueObjects.EventId;

/**
 * Caso de uso para archivar un evento.
 */
@ApplicationScoped
public class ArchiveEventHandler {

    private final IEventRepository eventRepository;
    private final EventDtoAssembler assembler;

    public ArchiveEventHandler() {
        this.eventRepository = null;
        this.assembler = null;
    }

    @Inject
    public ArchiveEventHandler(IEventRepository eventRepository, EventDtoAssembler assembler) {
        this.eventRepository = eventRepository;
        this.assembler = assembler;
    }

    /**
     * Archiva el evento.
     *
     * @param command identificador del evento.
     * @return evento archivado.
     */
    public EventDto execute(ArchiveEventCommand command) {
        if (command == null || command.eventId() == null || command.eventId().isBlank()) {
            throw new DomainException("El identificador del evento es obligatorio");
        }
        Event event = eventRepository.findById(EventId.of(command.eventId()))
                .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + command.eventId()));
        event.archive();
        eventRepository.save(event);
        return assembler.toDto(event);
    }
}
