package org.infoUFPS.Persistence.EntityConfigurations;

import jakarta.persistence.*;
import org.infoUFPS.Enums.Role;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounts",
        indexes = {
                @Index(name = "idx_accounts_email", columnList = "email", unique = true)
        })
public class AccountJpa {

    @Id
    @Column(name = "account_id", length = 36, nullable = false)
    private String accountId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 30, nullable = false)
    private Role role = Role.USER;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AccountJpa() {}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}
