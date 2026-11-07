package org.infoUFPS.Features.Account.Commands;

public record RegisterAccountCommand(
        String email,
        String password,
        String name
) {
    public RegisterAccountCommand {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("El email es obligatorio");
        if (password == null || password.length() < 8)
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio");
    }
}
