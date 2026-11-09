package org.infoUFPS.DTOs;

import org.infoUFPS.Entities.Account;
import org.infoUFPS.Enums.Role;

public record AccountDto(
        String accountId,
        String name,
        String email,
        Role role
) {
    public static AccountDto from(Account account) {
        return new AccountDto(
                account.getAccountId().value(),
                account.getName(),
                account.getEmail(),
                account.getRole()
        );
    }
}
