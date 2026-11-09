package org.infoUFPS.Features.Calendar.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.Entities.Event;
import org.infoUFPS.Entities.FavoriteEvent;
import org.infoUFPS.Enums.Status;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Exceptions.NotFoundException;
import org.infoUFPS.Repositories.IEventRepository;
import org.infoUFPS.Repositories.IFavoriteEventRepository;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.EventId;

/**
 * Caso de uso para marcar un evento como favorito.
 * La operación es idempotente y solo admite eventos publicados.
 */
@ApplicationScoped
public class AddFavoriteHandler {

    private final IFavoriteEventRepository favoriteRepository;
    private final IEventRepository eventRepository;

    public AddFavoriteHandler() {
        this.favoriteRepository = null;
        this.eventRepository = null;
    }

    @Inject
    public AddFavoriteHandler(IFavoriteEventRepository favoriteRepository, IEventRepository eventRepository) {
        this.favoriteRepository = favoriteRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Marca el evento como favorito.
     *
     * @param command cuenta y evento.
     */
    public void execute(AddFavoriteCommand command) {
        if (command == null || command.accountId() == null || command.accountId().isBlank()) {
            throw new DomainException("La cuenta es obligatoria para marcar un favorito");
        }
        if (command.eventId() == null || command.eventId().isBlank()) {
            throw new DomainException("El identificador del evento es obligatorio");
        }

        AccountId accountId = AccountId.of(command.accountId());
        EventId eventId = EventId.of(command.eventId());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + command.eventId()));
        if (event.getStatus() != Status.PUBLISHED) {
            throw new NotFoundException("Evento no encontrado: " + command.eventId());
        }

        if (!favoriteRepository.exists(accountId, eventId)) {
            favoriteRepository.save(FavoriteEvent.create(accountId, eventId));
        }
    }
}
