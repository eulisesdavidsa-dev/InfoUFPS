package org.infoUFPS.Security;

import org.infoUFPS.Exceptions.ForbiddenException;
import org.infoUFPS.Exceptions.UnauthorizedException;

/**
 * Identidad del solicitante mientras no existe login/JWT.
 * <p>
 * Punto único de aislamiento: hoy el id de cuenta y el rol llegan como parámetros
 * de la petición (cabeceras o query params). Cuando se implemente el login, solo
 * debe cambiarse la construcción de este objeto por la lectura del JWT.
 *
 * @param accountId identificador de la cuenta; puede ser null si no se envió.
 * @param admin     true si el rol es administrador.
 */
public record CurrentUser(String accountId, boolean admin) {

    /**
     * Construye la identidad a partir de los valores recibidos.
     *
     * @param accountId identificador de cuenta.
     * @param role      rol textual.
     * @return identidad normalizada.
     */
    public static CurrentUser from(String accountId, String role) {
        String normalizedAccount = accountId == null || accountId.isBlank() ? null : accountId.strip();
        boolean isAdmin = role != null
                && (role.strip().equalsIgnoreCase("ADMIN")
                || role.strip().equalsIgnoreCase("ROLE_ADMIN")
                || role.strip().equalsIgnoreCase("ADMINISTRADOR"));
        return new CurrentUser(normalizedAccount, isAdmin);
    }

    /**
     * Exige una cuenta autenticada.
     *
     * @return identificador de la cuenta.
     * @throws UnauthorizedException si no llegó la cuenta.
     */
    public String requireAccountId() {
        if (accountId == null || accountId.isBlank()) {
            throw new UnauthorizedException("Se requiere una cuenta autenticada");
        }
        return accountId;
    }

    /**
     * Exige el rol de administrador.
     *
     * @throws ForbiddenException si el rol no es administrador.
     */
    public void requireAdmin() {
        if (!admin) {
            throw new ForbiddenException("Se requiere rol de administrador");
        }
    }
}
