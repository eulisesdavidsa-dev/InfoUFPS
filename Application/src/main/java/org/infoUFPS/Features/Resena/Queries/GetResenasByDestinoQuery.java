package org.infoUFPS.Features.Resena.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.ResenaDto;
import org.infoUFPS.Entities.Account;
import org.infoUFPS.Entities.Resena;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.Repositories.IAccountRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.ValueObjects.MateriaId;
import org.infoUFPS.ValueObjects.ProfesorId;

import java.util.List;

@ApplicationScoped
public class GetResenasByDestinoQuery {

    private final IResenaRepository resenaRepository;
    private final IAccountRepository accountRepository;

    public GetResenasByDestinoQuery() {
        this.resenaRepository = null;
        this.accountRepository = null;
    }

    @Inject
    public GetResenasByDestinoQuery(IResenaRepository resenaRepository, IAccountRepository accountRepository) {
        this.resenaRepository = resenaRepository;
        this.accountRepository = accountRepository;
    }

    public PageResult<ResenaDto> getByProfesor(String profesorIdStr, PageRequest pageRequest) {
        ProfesorId profesorId = ProfesorId.of(profesorIdStr);
        PageResult<Resena> result = resenaRepository.findPublishedByProfesor(profesorId, pageRequest);

        List<ResenaDto> dtos = result.items().stream().map(r -> {
            String autorNombre = accountRepository.findById(r.getAutorId()).map(Account::getName).orElse("Usuario");
            return ResenaDto.of(r, autorNombre);
        }).toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }

    public PageResult<ResenaDto> getByMateria(String materiaIdStr, PageRequest pageRequest) {
        MateriaId materiaId = MateriaId.of(materiaIdStr);
        PageResult<Resena> result = resenaRepository.findPublishedByMateria(materiaId, pageRequest);

        List<ResenaDto> dtos = result.items().stream().map(r -> {
            String autorNombre = accountRepository.findById(r.getAutorId()).map(Account::getName).orElse("Usuario");
            return ResenaDto.of(r, autorNombre);
        }).toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }
}

