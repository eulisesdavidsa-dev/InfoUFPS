package org.infoUFPS.Entities;

import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.ValueObjects.AccountId;

import java.time.LocalDateTime;
import java.util.Objects;

public class Account {

    private AccountId accountId;
    private String name;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;

    private Account(AccountId accountId,String name, String email, String passwordHash) {
        this.accountId = Objects.requireNonNull(accountId, "accountId es obligatorio");
        this.name = Objects.requireNonNull(name, "nombre es obligatorio");
        this.email = Objects.requireNonNull(email, "email es obligatorio");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash es obligatorio");
        this.createdAt = LocalDateTime.now();
    }

    public static Account register(String email, String passwordHash, String name) {
        if (email == null || email.isBlank()) {
            throw new DomainException("El email es obligatorio");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DomainException("La contraseña es obligatoria");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de la cuenta es obligatorio");
        }
        return new Account(AccountId.generate(),name, email, passwordHash);
    }

    /**
     * Reconstrucción desde persistencia.
     */
    public static Account of(AccountId accountId, String name, String email,
                             String passwordHash, LocalDateTime createdAt) {
        Account account = new Account(accountId, name, email, passwordHash);
        account.createdAt = createdAt;
        return account;
    }

    public AccountId getAccountId() {return accountId;}
    public String getName() {return name;}
    public String getEmail() {return email;}
    public String getPasswordHash() {return passwordHash;}
    public LocalDateTime getCreatedAt() {return createdAt;}

}
