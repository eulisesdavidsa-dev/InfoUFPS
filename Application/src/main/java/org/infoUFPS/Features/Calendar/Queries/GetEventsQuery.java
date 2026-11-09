package org.infoUFPS.Features.Calendar.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.Criteria.EventRangeCriteria;
import org.infoUFPS.DTOs.EventDto;
import org.infoUFPS.Features.Calendar.Support.EventDtoAssembler;
import org.infoUFPS.Features.Calendar.Support.EventQueryValidator;
import org.infoUFPS.Repositories.IEventRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Consulta los eventos publicados que se solapan con un rango de fechas.
 */
@ApplicationScoped
public class GetEventsQuery {

    private final IEventRepository eventRepository;
    private final EventDtoAssembler assembler;

    public GetEventsQuery() {
        this.eventRepository = null;
        this.assembler = null;
    }

    @Inject
    public GetEventsQuery(IEventRepository eventRepository, EventDtoAssembler assembler) {
        this.eventRepository = eventRepository;
        this.assembler = assembler;
    }

    /**
     * Ejecuta la consulta de rango.
     *
     * @param from     fecha inicial.
     * @param to       fecha final.
     * @param rawTypes tipos a filtrar.
     * @return eventos publicados ordenados para presentación.
     */
    public List<EventDto> execute(LocalDate from, LocalDate to, List<String> rawTypes) {
        EventRangeCriteria criteria = EventQueryValidator.validateRange(from, to, rawTypes);
        return assembler.toDtos(eventRepository.findPublishedOverlapping(criteria));
    }
}
