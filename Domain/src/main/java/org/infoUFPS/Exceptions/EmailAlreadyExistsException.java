package org.infoUFPS.Exceptions;

public class EmailAlreadyExistsException extends DomainException {
    public EmailAlreadyExistsException(String email) {
        super("Ya existe una cuenta con ese email: " + email);
    }
}
