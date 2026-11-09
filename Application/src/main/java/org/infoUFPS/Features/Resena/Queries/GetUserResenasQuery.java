package org.infoUFPS.Features.Resena.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.ResenaDto;
import org.infoUFPS.Entities.Account;
import org.infoUFPS.Entities.Resena;
import org.infoUFPS.Repositories.IAccountRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.ValueObjects.AccountId;

import java.util.List;

@ApplicationScoped
public class GetUserResenasQuery {

    private final IResenaRepository resenaRepository;
    private final IAccountRepository accountRepository;

    public GetUserResenasQuery() {
        this.resenaRepository = null;
        this.accountRepository = null;
    }

    @Inject
    public GetUserResenasQuery(IResenaRepository resenaRepository, IAccountRepository accountRepository) {
        this.resenaRepository = resenaRepository;
        this.accountRepository = accountRepository;
    }

    public List<ResenaDto> execute(String autorIdStr) {
        AccountId autorId = AccountId.of(autorIdStr);
        String autorNombre = accountRepository.findById(autorId).map(Account::getName).orElse("Usuario");

        List<Resena> resenas = resenaRepository.findByAutorId(autorId);
        return resenas.stream()
                .map(r -> ResenaDto.of(r, autorNombre))
                .toList();
    }
}

