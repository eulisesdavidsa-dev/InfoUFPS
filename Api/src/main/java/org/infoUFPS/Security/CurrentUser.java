package org.infoUFPS.Security;

import org.infoUFPS.Exceptions.ForbiddenException;
import org.infoUFPS.Exceptions.UnauthorizedException;

/**
 * Identidad del solicitante mientras no existe login/JWT.
 * <p>
 * Punto único de aislamiento: hoy el id de cuenta llega como parámetro de la
 * petición y el rol se resuelve en {@link ActorContext}. Cuando se implemente el
 * login, solo debe cambiarse la construcción de este objeto por la lectura del
 * JWT.
 *
 * @param accountId identificador de la cuenta; puede ser null si no se envió.
 * @param admin     true si el rol resuelto es administrador.
 */
public record CurrentUser(String accountId, boolean admin) {

    /**
     * Identidad sin cuenta autenticada.
     */
    public static CurrentUser anonymous() {
        return new CurrentUser(null, false);
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
     * Exige el rol de administrador. Primero valida que exista una cuenta para
     * que una petición sin identidad produzca 401 y una cuenta sin permisos 403.
     *
     * @throws UnauthorizedException si no llegó la cuenta.
     * @throws ForbiddenException    si el rol no es administrador.
     */
    public void requireAdmin() {
        requireAccountId();
        if (!admin) {
            throw new ForbiddenException("Se requiere rol de administrador");
        }
    }
}
