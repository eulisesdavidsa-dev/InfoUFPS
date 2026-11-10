package org.infoUFPS.Exceptions;

/**
 * Excepción para recursos inexistentes. Hereda de {@link NotFoundException}
 * para que la capa Api la traduzca a HTTP 404.
 */
public class ResourceNotFoundException extends NotFoundException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
