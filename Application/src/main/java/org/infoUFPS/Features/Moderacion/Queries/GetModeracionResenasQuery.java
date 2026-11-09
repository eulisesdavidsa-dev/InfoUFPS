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

import java.util.List;

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

        List<ResenaDto> dtos = result.items().stream().map(r -> {
            String autorNombre = accountRepository.findById(r.getAutorId()).map(Account::getName).orElse("Usuario");
            return ResenaDto.of(r, autorNombre);
        }).toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }
}

