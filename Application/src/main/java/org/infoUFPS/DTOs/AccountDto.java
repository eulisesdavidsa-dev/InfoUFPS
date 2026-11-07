package org.infoUFPS.DTOs;

import org.infoUFPS.Entities.Account;

public record AccountDto(
        String accountId,
        String name,
        String email
) {
    public static AccountDto from(Account account) {
        return new AccountDto(
                account.getAccountId().value(),
                account.getName(),
                account.getEmail()
        );
    }
}
