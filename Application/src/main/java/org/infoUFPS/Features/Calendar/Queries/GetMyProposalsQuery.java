package org.infoUFPS.Features.Calendar.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.EventDto;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Features.Calendar.Support.EventDtoAssembler;
import org.infoUFPS.Repositories.IEventRepository;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.PageRequest;
import org.infoUFPS.ValueObjects.PagedResult;

/**
 * Consulta las propuestas de la cuenta autenticada.
 */
@ApplicationScoped
public class GetMyProposalsQuery {

    private final IEventRepository eventRepository;
    private final EventDtoAssembler assembler;

    public GetMyProposalsQuery() {
        this.eventRepository = null;
        this.assembler = null;
    }

    @Inject
    public GetMyProposalsQuery(IEventRepository eventRepository, EventDtoAssembler assembler) {
        this.eventRepository = eventRepository;
        this.assembler = assembler;
    }

    /**
     * Ejecuta la consulta.
     *
     * @param accountId cuenta autenticada.
     * @param page      página base cero.
     * @param size      tamaño de página.
     * @return página de propuestas.
     */
    public PagedResult<EventDto> execute(String accountId, int page, int size) {
        if (accountId == null || accountId.isBlank()) {
            throw new DomainException("La cuenta es obligatoria");
        }
        return assembler.toPagedResult(
                eventRepository.findByProposedBy(AccountId.of(accountId), PageRequest.of(page, size)));
    }
}
