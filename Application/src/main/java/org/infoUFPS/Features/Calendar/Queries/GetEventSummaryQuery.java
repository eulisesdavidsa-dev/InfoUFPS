package org.infoUFPS.Features.Calendar.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.Criteria.EventRangeCriteria;
import org.infoUFPS.DTOs.EventDaySummaryDto;
import org.infoUFPS.Entities.Event;
import org.infoUFPS.Features.Calendar.Support.EventQueryValidator;
import org.infoUFPS.Repositories.IEventRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Consulta el resumen de eventos publicados por día en un rango.
 * Un evento de varios días cuenta en cada día que abarca.
 */
@ApplicationScoped
public class GetEventSummaryQuery {

    private final IEventRepository eventRepository;

    public GetEventSummaryQuery() {
        this.eventRepository = null;
    }

    @Inject
    public GetEventSummaryQuery(IEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Ejecuta el resumen diario.
     *
     * @param from      fecha inicial inclusiva.
     * @param to        fecha final inclusiva.
     * @param rawTypes  tipos a filtrar.
     * @return conteo por cada día del rango, incluidos los días sin eventos.
     */
    public List<EventDaySummaryDto> execute(LocalDate from, LocalDate to, List<String> rawTypes) {
        EventRangeCriteria criteria = EventQueryValidator.validateRange(from, to, rawTypes);
        Map<LocalDate, Long> counts = new TreeMap<>();
        LocalDate cursor = criteria.from();
        while (!cursor.isAfter(criteria.to())) {
            counts.put(cursor, 0L);
            cursor = cursor.plusDays(1);
        }

        List<Event> events = eventRepository.findPublishedOverlapping(criteria);
        for (Event event : events) {
            LocalDate day = event.getStartDate().isBefore(criteria.from())
                    ? criteria.from()
                    : event.getStartDate();
            LocalDate last = event.getEndDate().isAfter(criteria.to())
                    ? criteria.to()
                    : event.getEndDate();
            while (!day.isAfter(last)) {
                counts.merge(day, 1L, Long::sum);
                day = day.plusDays(1);
            }
        }

        List<EventDaySummaryDto> result = new ArrayList<>(counts.size());
        counts.forEach((date, count) -> result.add(new EventDaySummaryDto(date, count)));
        return result;
    }
}
