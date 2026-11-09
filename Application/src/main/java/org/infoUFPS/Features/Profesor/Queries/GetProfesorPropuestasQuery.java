package org.infoUFPS.Features.Profesor.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.ProfesorDetailDto;
import org.infoUFPS.Entities.Profesor;
import org.infoUFPS.Repositories.IProfesorRepository;
import org.infoUFPS.ValueObjects.AccountId;

import java.util.List;

@ApplicationScoped
public class GetProfesorPropuestasQuery {

    private final IProfesorRepository profesorRepository;

    public GetProfesorPropuestasQuery() {
        this.profesorRepository = null;
    }

    @Inject
    public GetProfesorPropuestasQuery(IProfesorRepository profesorRepository) {
        this.profesorRepository = profesorRepository;
    }

    public List<ProfesorDetailDto> execute(String proponenteIdStr) {
        AccountId proponenteId = AccountId.of(proponenteIdStr);
        List<Profesor> profesores = profesorRepository.findByProponenteId(proponenteId);
        return profesores.stream()
                .map(p -> ProfesorDetailDto.of(p, null, 0L, List.of(), true))
                .toList();
    }
}

