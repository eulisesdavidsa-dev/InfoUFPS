package org.infoUFPS.Features.Calendar.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Repositories.IFavoriteEventRepository;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.EventId;

/**
 * Caso de uso para quitar un evento de favoritos.
 * La operación es idempotente.
 */
@ApplicationScoped
public class RemoveFavoriteHandler {

    private final IFavoriteEventRepository favoriteRepository;

    public RemoveFavoriteHandler() {
        this.favoriteRepository = null;
    }

    @Inject
    public RemoveFavoriteHandler(IFavoriteEventRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * Quita el favorito si existe.
     *
     * @param command cuenta y evento.
     */
    public void execute(RemoveFavoriteCommand command) {
        if (command == null || command.accountId() == null || command.accountId().isBlank()) {
            throw new DomainException("La cuenta es obligatoria para quitar un favorito");
        }
        if (command.eventId() == null || command.eventId().isBlank()) {
            throw new DomainException("El identificador del evento es obligatorio");
        }
        favoriteRepository.delete(AccountId.of(command.accountId()), EventId.of(command.eventId()));
    }
}
