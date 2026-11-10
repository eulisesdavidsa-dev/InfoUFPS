package org.infoUFPS.Features.Profesor.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.Criterias.ProfesorSearchCriteria;
import org.infoUFPS.DTOs.PublicProfesorListItemDto;
import org.infoUFPS.Entities.Profesor;
import org.infoUFPS.Enums.Facultad;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.Repositories.IProfesorRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.ValueObjects.ProfesorId;
import org.infoUFPS.ValueObjects.RatingSummary;

import java.util.List;
import java.util.Map;

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

    public PageResult<PublicProfesorListItemDto> execute(String query, Facultad facultad, PageRequest pageRequest) {
        ProfesorSearchCriteria criteria = ProfesorSearchCriteria.publicSearch(query, facultad);
        PageResult<Profesor> result = profesorRepository.search(criteria, pageRequest);

        List<ProfesorId> ids = result.items().stream().map(Profesor::getId).toList();
        Map<String, RatingSummary> summaries = resenaRepository.getRatingSummariesByProfesor(ids);

        List<PublicProfesorListItemDto> dtos = result.items().stream().map(p -> {
            RatingSummary summary = summaries.getOrDefault(p.getId().value(), RatingSummary.empty());
            return PublicProfesorListItemDto.of(p, summary.average(), summary.total());
        }).toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }
}
