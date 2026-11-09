package org.infoUFPS.Features.Profesor.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.Criterias.ProfesorSearchCriteria;
import org.infoUFPS.DTOs.ProfesorListItemDto;
import org.infoUFPS.Entities.Profesor;
import org.infoUFPS.Enums.Facultad;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.Repositories.IProfesorRepository;
import org.infoUFPS.Repositories.IResenaRepository;

import java.util.List;

@ApplicationScoped
public class GetProfesoresPublicQuery {

    private final IProfesorRepository profesorRepository;
    private final IResenaRepository resenaRepository;

    public GetProfesoresPublicQuery() {
        this.profesorRepository = null;
        this.resenaRepository = null;
    }

    @Inject
    public GetProfesoresPublicQuery(IProfesorRepository profesorRepository, IResenaRepository resenaRepository) {
        this.profesorRepository = profesorRepository;
        this.resenaRepository = resenaRepository;
    }

    public PageResult<ProfesorListItemDto> execute(String query, Facultad facultad, PageRequest pageRequest) {
        ProfesorSearchCriteria criteria = ProfesorSearchCriteria.publicSearch(query, facultad);
        PageResult<Profesor> result = profesorRepository.search(criteria, pageRequest);

        List<ProfesorListItemDto> dtos = result.items().stream().map(p -> {
            Double avg = resenaRepository.getAverageRatingForProfesor(p.getId());
            long total = resenaRepository.countPublishedByProfesor(p.getId());
            return ProfesorListItemDto.of(p, avg, total, true);
        }).toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }
}

