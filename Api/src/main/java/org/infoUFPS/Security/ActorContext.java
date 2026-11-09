package org.infoUFPS.Security;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;

/**
 * Contexto de identidad recibido por parámetro en cada petición.
 * <p>
 * Se aceptan cabeceras {@code X-Account-Id} y {@code X-Account-Role}, o los
 * query params {@code accountId} y {@code role} para pruebas y desarrollo.
 * Todo el resto de la aplicación consume {@link CurrentUser}; este es el único
 * lugar que debe reemplazarse cuando exista JWT.
 */
public class ActorContext {

    @HeaderParam("X-Account-Id")
    private String headerAccountId;

    @HeaderParam("X-Account-Role")
    private String headerRole;

    @QueryParam("accountId")
    private String queryAccountId;

    @QueryParam("role")
    private String queryRole;

    /**
     * @return identidad normalizada de la petición.
     */
    public CurrentUser currentUser() {
        String accountId = headerAccountId != null && !headerAccountId.isBlank()
                ? headerAccountId
                : queryAccountId;
        String role = headerRole != null && !headerRole.isBlank()
                ? headerRole
                : queryRole;
        return CurrentUser.from(accountId, role);
    }
}
