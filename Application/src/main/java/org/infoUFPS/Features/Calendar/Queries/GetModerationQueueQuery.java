package org.infoUFPS.Features.Calendar.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.Criteria.ModerationCriteria;
import org.infoUFPS.DTOs.EventDto;
import org.infoUFPS.DTOs.ModerationEventDto;
import org.infoUFPS.Enums.Status;
import org.infoUFPS.Features.Calendar.Support.EventDuplicateGuard;
import org.infoUFPS.Features.Calendar.Support.EventDtoAssembler;
import org.infoUFPS.Features.Calendar.Support.EventQueryValidator;
import org.infoUFPS.Repositories.IEventRepository;
import org.infoUFPS.ValueObjects.PageRequest;
import org.infoUFPS.ValueObjects.PagedResult;

import java.util.List;

/**
 * Consulta la cola de moderación por estado, incluyendo posibles duplicados.
 */
@ApplicationScoped
public class GetModerationQueueQuery {

    private final IEventRepository eventRepository;
    private final EventDtoAssembler assembler;
    private final EventDuplicateGuard duplicateGuard;

    public GetModerationQueueQuery() {
        this.eventRepository = null;
        this.assembler = null;
        this.duplicateGuard = null;
    }

    @Inject
    public GetModerationQueueQuery(IEventRepository eventRepository,
                                   EventDtoAssembler assembler,
                                   EventDuplicateGuard duplicateGuard) {
        this.eventRepository = eventRepository;
        this.assembler = assembler;
        this.duplicateGuard = duplicateGuard;
    }

    /**
     * Ejecuta la consulta de la cola.
     *
     * @param rawStatus estado solicitado; por defecto UNDER_REVIEW.
     * @param page      página base cero.
     * @param size      tamaño de página.
     * @return página de eventos con sus posibles duplicados.
     */
    public PagedResult<ModerationEventDto> execute(String rawStatus, int page, int size) {
        Status status = EventQueryValidator.parseStatus(rawStatus);
        PagedResult<org.infoUFPS.Entities.Event> result = eventRepository.findByStatus(
                new ModerationCriteria(status, PageRequest.of(page, size)));

        return result.map(event -> {
            EventDto dto = assembler.toDto(event);
            List<EventDto> duplicates = assembler.toDtos(duplicateGuard.findPossibleDuplicates(event));
            return new ModerationEventDto(dto, duplicates);
        });
    }
}
