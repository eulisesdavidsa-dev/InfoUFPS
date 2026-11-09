package org.infoUFPS.Persistence.Repositories;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.infoUFPS.Entities.Account;
import org.infoUFPS.Persistence.EntityConfigurations.AccountJpa;
import org.infoUFPS.Repositories.IAccountRepository;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.ContentId;

import java.util.Optional;

@ApplicationScoped
public class AccountRepositoryJpa implements IAccountRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Account save(Account account) {
        // Guardar o actualizar la cuenta principal
        AccountJpa accountJpa = toEntity(account);
        entityManager.merge(accountJpa);
        return account;
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        AccountJpa accountJpa = entityManager
                .createQuery("SELECT a FROM AccountJpa a WHERE a.email = :email", AccountJpa.class)
                .setParameter("email", email)
                .getResultList().stream().findFirst().orElse(null);

        if (accountJpa == null) {
            return Optional.empty();
        }

        return Optional.of(toDomain(accountJpa));
    }

    @Override
    public boolean existsByEmail(String email) {
        Long count = entityManager
                .createQuery("SELECT COUNT(a) FROM AccountJpa a WHERE a.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public Optional<Account> findById(AccountId accountId) {
        AccountJpa accountJpa = entityManager
                .createQuery("SELECT a FROM AccountJpa a WHERE a.accountId = :id", AccountJpa.class)
                .setParameter("id", accountId.value())
                .getResultList().stream().findFirst().orElse(null);

        if (accountJpa == null) {
            return Optional.empty();
        }

        return Optional.of(toDomain(accountJpa));
    }

    @Override
    @Transactional
    public void deleteById(AccountId id){
        entityManager.createQuery(
                        "DELETE FROM AccountJpa cj WHERE cj.accountId = :id")
                .setParameter("id", id.value())
                .executeUpdate();
    }

    // ── Mapeo: Domain → JPA ──────────────────────────────────────────

    private AccountJpa toEntity(Account account) {
        AccountJpa entity = new AccountJpa();
        entity.setAccountId(account.getAccountId().value());
        entity.setName(account.getName());
        entity.setEmail(account.getEmail());
        entity.setPasswordHash(account.getPasswordHash());
        entity.setRole(account.getRole());
        entity.setCreatedAt(account.getCreatedAt());
        return entity;
    }

    // ── Mapeo: JPA → Domain ──────────────────────────────────────────

    private Account toDomain(AccountJpa accountJpa) {
        return Account.of(
                AccountId.of(accountJpa.getAccountId()),
                accountJpa.getName(),
                accountJpa.getEmail(),
                accountJpa.getPasswordHash(),
                accountJpa.getRole(),
                accountJpa.getCreatedAt()
        );
    }

}
