package org.infoUFPS.Features.Resena.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.PublicResenaDto;
import org.infoUFPS.Entities.Account;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Entities.Profesor;
import org.infoUFPS.Entities.Resena;
import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Exceptions.NotFoundException;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.Repositories.IAccountRepository;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.Repositories.IProfesorRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.MateriaId;
import org.infoUFPS.ValueObjects.ProfesorId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetResenasByDestinoQuery {

    private final IResenaRepository resenaRepository;
    private final IAccountRepository accountRepository;
    private final IProfesorRepository profesorRepository;
    private final IMateriaRepository materiaRepository;

    public GetResenasByDestinoQuery() {
        this.resenaRepository = null;
        this.accountRepository = null;
        this.profesorRepository = null;
        this.materiaRepository = null;
    }

    @Inject
    public GetResenasByDestinoQuery(IResenaRepository resenaRepository,
                                    IAccountRepository accountRepository,
                                    IProfesorRepository profesorRepository,
                                    IMateriaRepository materiaRepository) {
        this.resenaRepository = resenaRepository;
        this.accountRepository = accountRepository;
        this.profesorRepository = profesorRepository;
        this.materiaRepository = materiaRepository;
    }

    public PageResult<PublicResenaDto> getByProfesor(String profesorIdStr, PageRequest pageRequest) {
        ProfesorId profesorId = ProfesorId.of(profesorIdStr);
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new NotFoundException("No se encontró el profesor con ID: " + profesorIdStr));
        if (profesor.getEstado() != EstadoModeracion.PUBLICADO) {
            throw new NotFoundException("El profesor no está disponible o no ha sido publicado");
        }

        PageResult<Resena> result = resenaRepository.findPublishedByProfesor(profesorId, pageRequest);
        return toDtoPage(result);
    }

    public PageResult<PublicResenaDto> getByMateria(String materiaIdStr, PageRequest pageRequest) {
        MateriaId materiaId = MateriaId.of(materiaIdStr);
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new NotFoundException("No se encontró la materia con ID: " + materiaIdStr));
        if (materia.getEstado() != EstadoModeracion.PUBLICADO) {
            throw new NotFoundException("La materia no está disponible o no ha sido publicada");
        }

        PageResult<Resena> result = resenaRepository.findPublishedByMateria(materiaId, pageRequest);
        return toDtoPage(result);
    }

    private PageResult<PublicResenaDto> toDtoPage(PageResult<Resena> result) {
        List<AccountId> autorIds = result.items().stream()
                .map(Resena::getAutorId)
                .distinct()
                .toList();
        Map<String, String> nombres = accountRepository.findByIds(autorIds).stream()
                .collect(Collectors.toMap(
                        account -> account.getAccountId().value(),
                        Account::getName,
                        (a, b) -> a));

        List<PublicResenaDto> dtos = result.items().stream()
                .map(r -> PublicResenaDto.of(r, nombres.getOrDefault(r.getAutorId().value(), "Usuario")))
                .toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }
}
