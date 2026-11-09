package org.infoUFPS.ValueObjects;

import java.time.LocalDate;

/**
 * Conteo de eventos publicados para un día concreto del calendario.
 *
 * @param date  día del resumen.
 * @param count cantidad de eventos publicados que cubren ese día.
 */
public record EventDaySummary(LocalDate date, long count) {
}
