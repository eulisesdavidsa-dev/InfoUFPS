package org.infoUFPS.Security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.infoUFPS.Entities.Account;
import org.infoUFPS.Enums.Role;
import org.infoUFPS.ExceptionMappers.ErrorResponse;
import org.infoUFPS.Repositories.IAccountRepository;
import org.infoUFPS.ValueObjects.AccountId;

import java.util.Optional;

@RequestScoped
public class AuthContext {

    @Context
    HttpHeaders httpHeaders;

    @Inject
    IAccountRepository accountRepository;

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLE_HEADER = "X-User-Role";

    public Optional<String> getUserId() {
        if (httpHeaders == null) {
            return Optional.empty();
        }
        String header = httpHeaders.getHeaderString(USER_ID_HEADER);
        if (header != null && !header.isBlank()) {
            return Optional.of(header.trim());
        }
        return Optional.empty();
    }

    public Role getRole() {
        if (httpHeaders != null) {
            String roleHeader = httpHeaders.getHeaderString(USER_ROLE_HEADER);
            if (roleHeader != null && !roleHeader.isBlank()) {
                try {
                    return Role.valueOf(roleHeader.trim().toUpperCase());
                } catch (IllegalArgumentException ignored) {}
            }
        }

        Optional<String> userIdOpt = getUserId();
        if (userIdOpt.isPresent() && accountRepository != null) {
            try {
                return accountRepository.findById(AccountId.of(userIdOpt.get()))
                        .map(Account::getRole)
                        .orElse(Role.USER);
            } catch (Exception ignored) {}
        }
        return Role.USER;
    }

    public boolean isAdmin() {
        return getRole() == Role.ADMIN;
    }

    public String requireAuthenticatedUserId() {
        return getUserId().orElseThrow(() -> new WebApplicationException(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.of("UNAUTHORIZED", "Autenticación requerida. Proporcione la cabecera " + USER_ID_HEADER, 401))
                        .build()
        ));
    }

    public void requireAdmin() {
        requireAuthenticatedUserId();
        if (!isAdmin()) {
            throw new WebApplicationException(
                    Response.status(Response.Status.FORBIDDEN)
                            .entity(ErrorResponse.of("FORBIDDEN", "Se requieren permisos de administrador", 403))
                            .build()
            );
        }
    }
}
