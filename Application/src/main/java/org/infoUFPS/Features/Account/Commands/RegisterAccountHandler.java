package org.infoUFPS.Features.Account.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoUFPS.Entities.Account;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Exceptions.EmailAlreadyExistsException;
import org.infoUFPS.Interfaces.PasswordHasher;
import org.infoUFPS.Repositories.IAccountRepository;
import org.infoUFPS.ValueObjects.Email;

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
     * Ejecuta el registro: normaliza y valida el email, comprueba su unicidad,
     * hashea la contraseña y crea la cuenta con perfil inicial.
     */
    @Transactional
    public Account execute(RegisterAccountCommand command) {
        String emailNorm = Email.normalize(command.email());
        if (emailNorm == null) {
            throw new DomainException("El email es obligatorio");
        }

        // Verificar email único (comparación sin distinguir mayúsculas/minúsculas)
        if (accountRepository.existsByEmail(emailNorm)) {
            throw new EmailAlreadyExistsException(emailNorm);
        }

        // Hashear contraseña con BCrypt
        String passwordHash = passwordHasher.hash(command.password());

        // Crear Account con perfil inicial
        Account account = Account.register(emailNorm, passwordHash, command.name());

        // Persistir
        return accountRepository.save(account);
    }
}
