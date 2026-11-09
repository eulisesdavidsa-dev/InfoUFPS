package org.infoUFPS.DTOs;

import org.infoUFPS.ValueObjects.EventDaySummary;

import java.time.LocalDate;

/**
 * DTO del resumen diario de eventos publicados.
 *
 * @param date  día.
 * @param count cantidad de eventos publicados que cubren el día.
 */
public record EventDaySummaryDto(LocalDate date, long count) {

    public static EventDaySummaryDto from(EventDaySummary summary) {
        return new EventDaySummaryDto(summary.date(), summary.count());
    }
}
