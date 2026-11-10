package org.infoUFPS.Features.Account.Commands;

import org.infoUFPS.Exceptions.DomainException;

public record RegisterAccountCommand(
        String email,
        String password,
        String name
) {
    public RegisterAccountCommand {
        if (email == null || email.isBlank())
            throw new DomainException("El email es obligatorio");
        if (password == null || password.length() < 8)
            throw new DomainException("La contraseña debe tener al menos 8 caracteres");
        if (name == null || name.isBlank())
            throw new DomainException("El nombre es obligatorio");
    }
    
}
