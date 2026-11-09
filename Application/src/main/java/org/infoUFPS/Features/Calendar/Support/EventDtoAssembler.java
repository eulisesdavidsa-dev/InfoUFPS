package org.infoUFPS.Features.Calendar.Support;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.EventDto;
import org.infoUFPS.Entities.Account;
import org.infoUFPS.Entities.Event;
import org.infoUFPS.Repositories.IAccountRepository;
import org.infoUFPS.Utils.BogotaTime;
import org.infoUFPS.ValueObjects.PagedResult;

import java.time.LocalDate;
import java.util.List;

/**
 * Ensambla DTOs de eventos resolviendo el nombre visible del autor.
 * El nombre es "Admin" cuando el evento fue creado por un administrador; en caso
 * contrario se usa el nombre de la cuenta.
 */
@ApplicationScoped
public class EventDtoAssembler {

    private final IAccountRepository accountRepository;

    public EventDtoAssembler() {
        this.accountRepository = null;
    }

    @Inject
    public EventDtoAssembler(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Convierte un evento a DTO usando la fecha actual de Bogotá.
     *
     * @param event evento de dominio.
     * @return DTO del evento.
     */
    public EventDto toDto(Event event) {
        return toDto(event, BogotaTime.today());
    }

    /**
     * Convierte un evento a DTO con una fecha de referencia.
     *
     * @param event         evento de dominio.
     * @param referenceDate fecha de referencia.
     * @return DTO del evento.
     */
    public EventDto toDto(Event event, LocalDate referenceDate) {
        return EventDto.from(event, resolveAuthorName(event), referenceDate);
    }

    /**
     * Convierte una lista de eventos a DTOs.
     *
     * @param events eventos de dominio.
     * @return DTOs.
     */
    public List<EventDto> toDtos(List<Event> events) {
        return events.stream().map(this::toDto).toList();
    }

    /**
     * Convierte una página de eventos a una página de DTOs.
     *
     * @param page página de eventos.
     * @return página de DTOs.
     */
    public PagedResult<EventDto> toPagedResult(PagedResult<Event> page) {
        return page.map(this::toDto);
    }

    private String resolveAuthorName(Event event) {
        if (event.isCreatedByAdmin()) {
            return "Admin";
        }
        if (accountRepository == null) {
            return "Usuario";
        }
        return accountRepository.findById(event.getProposedBy())
                .map(Account::getName)
                .orElse("Usuario");
    }
}
