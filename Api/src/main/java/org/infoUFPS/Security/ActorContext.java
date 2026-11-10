package org.infoUFPS.Security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;
import org.infoUFPS.Enums.Role;
import org.infoUFPS.Repositories.IAccountRepository;
import org.infoUFPS.ValueObjects.AccountId;

/**
 * Contexto de identidad de la petición.
 * <p>
 * Mientras no exista login/JWT, la cuenta se recibe por la cabecera
 * {@code X-Account-Id} (o el query param {@code accountId} para desarrollo).
 * El rol nunca se toma del cliente: se resuelve contra
 * {@link IAccountRepository} a partir del identificador de cuenta.
 * <p>
 * TODO(JWT): cuando se implemente autenticación, reemplazar la lectura de la
 * cabecera/query param por los claims del token y eliminar la resolución manual
 * del rol. Este es el único punto que debe cambiar.
 */
@RequestScoped
public class ActorContext {

    static final String ACCOUNT_ID_HEADER = "X-Account-Id";
    static final String LEGACY_ACCOUNT_ID_HEADER = "X-User-Id";
    static final String ACCOUNT_ID_QUERY_PARAM = "accountId";

    @Context
    HttpHeaders httpHeaders;

    @Context
    UriInfo uriInfo;

    @Inject
    IAccountRepository accountRepository;

    /**
     * @return identidad normalizada de la petición. Nunca contiene un rol
     * proporcionado por el cliente.
     */
    public CurrentUser currentUser() {
        String accountId = resolveAccountId();
        if (accountId == null) {
            return CurrentUser.anonymous();
        }

        boolean admin = accountRepository.findById(AccountId.of(accountId))
                .map(account -> account.getRole() == Role.ADMIN)
                .orElse(false);
        return new CurrentUser(accountId, admin);
    }

    private String resolveAccountId() {
        String headerAccountId = httpHeaders != null
                ? firstNonBlank(
                        httpHeaders.getHeaderString(ACCOUNT_ID_HEADER),
                        httpHeaders.getHeaderString(LEGACY_ACCOUNT_ID_HEADER))
                : null;
        if (headerAccountId != null) {
            return headerAccountId;
        }
        if (uriInfo != null) {
            return firstNonBlank(uriInfo.getQueryParameters().getFirst(ACCOUNT_ID_QUERY_PARAM));
        }
        return null;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.strip();
            }
        }
        return null;
    }
}
