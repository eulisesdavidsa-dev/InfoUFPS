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
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.EventId;

/**
 * Caso de uso para editar un evento o corregir una propuesta.
 * La revisión se incrementa y {@code publishedAt} se conserva.
 */
@ApplicationScoped
public class UpdateEventHandler {

    private final IEventRepository eventRepository;
    private final EventDuplicateGuard duplicateGuard;
    private final EventDtoAssembler assembler;

    public UpdateEventHandler() {
        this.eventRepository = null;
        this.duplicateGuard = null;
        this.assembler = null;
    }

    @Inject
    public UpdateEventHandler(IEventRepository eventRepository,
                              EventDuplicateGuard duplicateGuard,
                              EventDtoAssembler assembler) {
        this.eventRepository = eventRepository;
        this.duplicateGuard = duplicateGuard;
        this.assembler = assembler;
    }

    /**
     * Ejecuta la edición.
     *
     * @param command datos de la edición.
     * @return evento actualizado.
     */
    public EventDto execute(UpdateEventCommand command) {
        if (command == null || command.data() == null) {
            throw new DomainException("Los datos del evento son obligatorios");
        }
        if (command.eventId() == null || command.eventId().isBlank()) {
            throw new DomainException("El identificador del evento es obligatorio");
        }
        if (command.accountId() == null || command.accountId().isBlank()) {
            throw new DomainException("La cuenta es obligatoria para editar un evento");
        }

        Event event = eventRepository.findById(EventId.of(command.eventId()))
                .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + command.eventId()));

        EventRequest data = command.data();
        event.update(
                data.title(),
                data.toEventType(),
                data.location(),
                data.toSchedule(),
                data.description(),
                data.infoUrl(),
                AccountId.of(command.accountId()),
                command.admin()
        );

        duplicateGuard.ensureNoPublishedDuplicate(
                event.getTitle(), event.getStartDate(), event.getStartTime(), event.getId());

        eventRepository.save(event);
        return assembler.toDto(event);
    }
}
