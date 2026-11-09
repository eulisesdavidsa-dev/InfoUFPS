package org.infoUFPS.Features.Calendar.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.SharePageDto;
import org.infoUFPS.Entities.Event;
import org.infoUFPS.Enums.Status;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Exceptions.NotFoundException;
import org.infoUFPS.Features.Calendar.Support.EventShareSupport;
import org.infoUFPS.Features.Calendar.Support.HtmlEscaper;
import org.infoUFPS.Interfaces.CalendarSettings;
import org.infoUFPS.Repositories.IEventRepository;
import org.infoUFPS.ValueObjects.EventId;

/**
 * Genera la página HTML de vista previa para compartir un evento.
 * El título, la descripción y el lugar se escapan porque son escritos por usuarios.
 */
@ApplicationScoped
public class GetSharePageQuery {

    private final IEventRepository eventRepository;
    private final CalendarSettings settings;

    public GetSharePageQuery() {
        this.eventRepository = null;
        this.settings = null;
    }

    @Inject
    public GetSharePageQuery(IEventRepository eventRepository, CalendarSettings settings) {
        this.eventRepository = eventRepository;
        this.settings = settings;
    }

    /**
     * Ejecuta la generación de la página.
     *
     * @param eventId identificador del evento.
     * @return HTML de vista previa.
     */
    public SharePageDto execute(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new DomainException("El identificador del evento es obligatorio");
        }
        Event event = eventRepository.findById(EventId.of(eventId))
                .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + eventId));
        if (event.getStatus() != Status.PUBLISHED) {
            throw new NotFoundException("Evento no encontrado: " + eventId);
        }

        String eventUrl = HtmlEscaper.escape(EventShareSupport.eventUrl(event, settings));
        String logoUrl = HtmlEscaper.escape(settings.shareLogoUrl());
        String title = HtmlEscaper.escape(event.getTitle());
        String description = HtmlEscaper.escape(EventShareSupport.description(event));

        String html = "<!DOCTYPE html>\n"
                + "<html lang=\"es\">\n"
                + "<head>\n"
                + "  <meta charset=\"UTF-8\">\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "  <title>" + title + " | InfoUFPS</title>\n"
                + "  <meta property=\"og:type\" content=\"article\">\n"
                + "  <meta property=\"og:title\" content=\"" + title + "\">\n"
                + "  <meta property=\"og:description\" content=\"" + description + "\">\n"
                + "  <meta property=\"og:image\" content=\"" + logoUrl + "\">\n"
                + "  <meta property=\"og:url\" content=\"" + eventUrl + "\">\n"
                + "  <meta name=\"twitter:card\" content=\"summary_large_image\">\n"
                + "  <meta name=\"twitter:title\" content=\"" + title + "\">\n"
                + "  <meta name=\"twitter:description\" content=\"" + description + "\">\n"
                + "  <meta name=\"twitter:image\" content=\"" + logoUrl + "\">\n"
                + "  <meta http-equiv=\"refresh\" content=\"1;url=" + eventUrl + "\">\n"
                + "</head>\n"
                + "<body>\n"
                + "  <main>\n"
                + "    <img src=\"" + logoUrl + "\" alt=\"InfoUFPS\" width=\"96\" height=\"96\">\n"
                + "    <h1>" + title + "</h1>\n"
                + "    <p>" + description + "</p>\n"
                + "    <p><a href=\"" + eventUrl + "\">Ir al evento</a></p>\n"
                + "  </main>\n"
                + "</body>\n"
                + "</html>\n";

        return new SharePageDto(html);
    }
}
