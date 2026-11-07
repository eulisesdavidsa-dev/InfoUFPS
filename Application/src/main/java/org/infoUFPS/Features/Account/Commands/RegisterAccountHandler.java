package org.infoUFPS.Features.Account.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.Entities.Account;
import org.infoUFPS.Exceptions.EmailAlreadyExistsException;
import org.infoUFPS.Interfaces.PasswordHasher;
import org.infoUFPS.Repositories.IAccountRepository;

@ApplicationScoped
public class RegisterAccountHandler {

    private final IAccountRepository accountRepository;
    private final PasswordHasher passwordHasher;

    public RegisterAccountHandler() {
        this.accountRepository = null;
        this.passwordHasher = null;
    }

    @Inject
    public RegisterAccountHandler(IAccountRepository accountRepository, PasswordHasher passwordHasher) {
        this.accountRepository = accountRepository;
        this.passwordHasher = passwordHasher;
    }

    /**
     * Ejecuta el registro: valida email único, hashea contraseña, crea Account con perfil inicial.
     */
    public Account execute(RegisterAccountCommand command) {
        // Verificar email único
        if (accountRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        // Hashear contraseña con BCrypt
        String passwordHash = passwordHasher.hash(command.password());

        // Crear Account con perfil inicial
        Account account = Account.register(command.email(), passwordHash, command.name());

        // Persistir
        return accountRepository.save(account);
    }
}
