package org.infoUFPS.Repositories;

import org.infoUFPS.Entities.Account;
import org.infoUFPS.ValueObjects.AccountId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IAccountRepository {

    Account save(Account account);

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Account> findById(AccountId accountId);

    List<Account> findByIds(Collection<AccountId> accountIds);

    void deleteById(AccountId id);
}
