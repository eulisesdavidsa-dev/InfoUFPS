package org.infoUFPS.Features.Moderacion.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.ResenaDto;
import org.infoUFPS.Entities.Account;
import org.infoUFPS.Entities.Resena;
import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Enums.TipoDestino;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.Repositories.IAccountRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.ValueObjects.AccountId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetModeracionResenasQuery {

    private final IResenaRepository resenaRepository;
    private final IAccountRepository accountRepository;

    public GetModeracionResenasQuery() {
        this.resenaRepository = null;
        this.accountRepository = null;
    }

    @Inject
    public GetModeracionResenasQuery(IResenaRepository resenaRepository, IAccountRepository accountRepository) {
        this.resenaRepository = resenaRepository;
        this.accountRepository = accountRepository;
    }

    public PageResult<ResenaDto> execute(TipoDestino destino, EstadoModeracion estado, PageRequest pageRequest) {
        PageResult<Resena> result = resenaRepository.searchModeration(destino, estado, pageRequest);

        List<AccountId> autorIds = result.items().stream()
                .map(Resena::getAutorId)
                .distinct()
                .toList();
        Map<String, String> nombres = accountRepository.findByIds(autorIds).stream()
                .collect(Collectors.toMap(
                        account -> account.getAccountId().value(),
                        Account::getName,
                        (a, b) -> a));

        List<ResenaDto> dtos = result.items().stream()
                .map(r -> ResenaDto.of(r, nombres.getOrDefault(r.getAutorId().value(), "Usuario")))
                .toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }
}
