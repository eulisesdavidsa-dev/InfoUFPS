package org.infoUFPS.Entities;

import org.infoUFPS.Enums.Role;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.ValueObjects.AccountId;

import java.time.LocalDateTime;
import java.util.Objects;

public class Account {

    private AccountId accountId;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private LocalDateTime createdAt;

    private Account(AccountId accountId, String name, String email, String passwordHash, Role role) {
        this.accountId = Objects.requireNonNull(accountId, "accountId es obligatorio");
        this.name = Objects.requireNonNull(name, "nombre es obligatorio");
        this.email = Objects.requireNonNull(email, "email es obligatorio");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash es obligatorio");
        this.role = role != null ? role : Role.USER;
        this.createdAt = LocalDateTime.now();
    }

    public static Account register(String email, String passwordHash, String name) {
        return register(email, passwordHash, name, Role.USER);
    }

    public static Account register(String email, String passwordHash, String name, Role role) {
        if (email == null || email.isBlank()) {
            throw new DomainException("El email es obligatorio");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DomainException("La contraseña es obligatoria");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de la cuenta es obligatorio");
        }
        return new Account(AccountId.generate(), name, email, passwordHash, role);
    }

    /**
     * Reconstrucción desde persistencia.
     */
    public static Account of(AccountId accountId, String name, String email,
                             String passwordHash, LocalDateTime createdAt) {
        return of(accountId, name, email, passwordHash, Role.USER, createdAt);
    }

    public static Account of(AccountId accountId, String name, String email,
                             String passwordHash, Role role, LocalDateTime createdAt) {
        Account account = new Account(accountId, name, email, passwordHash, role);
        account.createdAt = createdAt;
        return account;
    }

    public AccountId getAccountId() { return accountId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }

}
