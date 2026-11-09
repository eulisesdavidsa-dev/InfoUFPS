package org.infoUFPS.Criterias;

import org.infoUFPS.Enums.Status;
import org.infoUFPS.ValueObjects.PageRequest;

/**
 * Criterio de la cola de moderación.
 *
 * @param status      estado a consultar.
 * @param pageRequest paginación solicitada.
 */
public record ModerationCriteria(Status status, PageRequest pageRequest) {

    public ModerationCriteria {
        status = status != null ? status : Status.UNDER_REVIEW;
        pageRequest = pageRequest != null ? pageRequest : PageRequest.first();
    }
}
