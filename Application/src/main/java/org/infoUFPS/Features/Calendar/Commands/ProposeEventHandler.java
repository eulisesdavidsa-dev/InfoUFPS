package org.infoUFPS.Features.Calendar.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.EventDto;
import org.infoUFPS.Entities.Event;
import org.infoUFPS.Exceptions.ConflictException;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Features.Calendar.Support.EventDuplicateGuard;
import org.infoUFPS.Features.Calendar.Support.EventDtoAssembler;
import org.infoUFPS.Interfaces.CalendarSettings;
import org.infoUFPS.Repositories.IEventRepository;
import org.infoUFPS.Utils.BogotaTime;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.EventSchedule;

/**
 * Caso de uso para que un usuario proponga un evento.
 * La propuesta entra en estado UNDER_REVIEW y queda sujeta al límite diario.
 */
@ApplicationScoped
public class ProposeEventHandler {

    private final IEventRepository eventRepository;
    private final EventDuplicateGuard duplicateGuard;
    private final EventDtoAssembler assembler;
    private final CalendarSettings settings;

    public ProposeEventHandler() {
        this.eventRepository = null;
        this.duplicateGuard = null;
        this.assembler = null;
        this.settings = null;
    }

    @Inject
    public ProposeEventHandler(IEventRepository eventRepository,
                               EventDuplicateGuard duplicateGuard,
                               EventDtoAssembler assembler,
                               CalendarSettings settings) {
        this.eventRepository = eventRepository;
        this.duplicateGuard = duplicateGuard;
        this.assembler = assembler;
        this.settings = settings;
    }

    /**
     * Ejecuta la propuesta de un evento.
     *
     * @param command datos de la propuesta.
     * @return evento creado en revisión.
     */
    public EventDto execute(ProposeEventCommand command) {
        if (command == null || command.data() == null) {
            throw new DomainException("Los datos del evento son obligatorios");
        }
        if (command.accountId() == null || command.accountId().isBlank()) {
            throw new DomainException("La cuenta es obligatoria para proponer un evento");
        }

        AccountId accountId = AccountId.of(command.accountId());
        EventRequest data = command.data();
        EventSchedule schedule = data.toSchedule();

        Event event = Event.create(
                data.title(),
                data.toEventType(),
                data.location(),
                schedule,
                data.description(),
                data.infoUrl(),
                accountId,
                false
        );

        duplicateGuard.ensureNoPublishedDuplicate(
                event.getTitle(), event.getStartDate(), event.getStartTime(), event.getId());

        int limit = settings != null ? settings.dailyProposalLimit() : 5;
        long proposalsToday = eventRepository.countProposalsByAccountOnDate(accountId, BogotaTime.today());
        if (proposalsToday >= limit) {
            throw new ConflictException("Has alcanzado el límite diario de propuestas (" + limit + ")");
        }

        eventRepository.save(event);
        return assembler.toDto(event);
    }
}
