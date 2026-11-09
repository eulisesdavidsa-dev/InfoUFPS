package org.infoUFPS.Features.Calendar.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.Criteria.EventPageCriteria;
import org.infoUFPS.DTOs.EventDto;
import org.infoUFPS.Features.Calendar.Support.EventDtoAssembler;
import org.infoUFPS.Features.Calendar.Support.EventQueryValidator;
import org.infoUFPS.Repositories.IEventRepository;
import org.infoUFPS.Utils.BogotaTime;
import org.infoUFPS.ValueObjects.PageRequest;
import org.infoUFPS.ValueObjects.PagedResult;

import java.util.List;

/**
 * Consulta los eventos publicados próximos o en curso, de forma paginada.
 */
@ApplicationScoped
public class GetUpcomingEventsQuery {

    private final IEventRepository eventRepository;
    private final EventDtoAssembler assembler;

    public GetUpcomingEventsQuery() {
        this.eventRepository = null;
        this.assembler = null;
    }

    @Inject
    public GetUpcomingEventsQuery(IEventRepository eventRepository, EventDtoAssembler assembler) {
        this.eventRepository = eventRepository;
        this.assembler = assembler;
    }

    /**
     * Ejecuta la consulta de próximos y en curso.
     *
     * @param rawTypes tipos a filtrar.
     * @param page     página base cero.
     * @param size     tamaño de página.
     * @return página de eventos.
     */
    public PagedResult<EventDto> execute(List<String> rawTypes, int page, int size) {
        EventPageCriteria criteria = new EventPageCriteria(
                EventQueryValidator.parseTypes(rawTypes),
                BogotaTime.today(),
                PageRequest.of(page, size)
        );
        return assembler.toPagedResult(eventRepository.findPublishedUpcomingOrOngoing(criteria));
    }
}
