package org.infoUFPS.Exceptions;

/**
 * Excepción de dominio para recursos inexistentes.
 * La capa Api la traduce a HTTP 404.
 */
public class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(message);
    }
}
