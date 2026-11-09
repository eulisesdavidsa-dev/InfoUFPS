package org.infoUFPS.Features.Calendar.Commands;

/**
 * Comando para que un administrador cree un evento publicado.
 *
 * @param data datos del evento.
 */
public record CreateEventCommand(EventRequest data) {
}
