package org.infoUFPS.Features.Calendar.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.PublicEventDto;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Features.Calendar.Support.EventDtoAssembler;
import org.infoUFPS.Repositories.IFavoriteEventRepository;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.PageRequest;
import org.infoUFPS.ValueObjects.PagedResult;

/**
 * Consulta los eventos favoritos publicados de la cuenta autenticada.
 */
@ApplicationScoped
public class GetMyFavoritesQuery {

    private final IFavoriteEventRepository favoriteRepository;
    private final EventDtoAssembler assembler;

    public GetMyFavoritesQuery() {
        this.favoriteRepository = null;
        this.assembler = null;
    }

    @Inject
    public GetMyFavoritesQuery(IFavoriteEventRepository favoriteRepository, EventDtoAssembler assembler) {
        this.favoriteRepository = favoriteRepository;
        this.assembler = assembler;
    }

    /**
     * Ejecuta la consulta.
     *
     * @param accountId cuenta autenticada.
     * @param page      página base cero.
     * @param size      tamaño de página.
     * @return página de eventos favoritos ordenados por fecha del evento.
     */
    public PagedResult<PublicEventDto> execute(String accountId, int page, int size) {
        if (accountId == null || accountId.isBlank()) {
            throw new DomainException("La cuenta es obligatoria");
        }
        return assembler.toPublicPagedResult(favoriteRepository.findPublishedFavoriteEvents(
                AccountId.of(accountId), PageRequest.of(page, size)));
    }
}
