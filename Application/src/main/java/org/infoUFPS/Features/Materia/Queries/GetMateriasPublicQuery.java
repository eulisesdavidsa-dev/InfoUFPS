package org.infoUFPS.Features.Materia.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.Criterias.MateriaSearchCriteria;
import org.infoUFPS.DTOs.PublicMateriaListItemDto;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Enums.TipoMateria;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.ValueObjects.MateriaId;
import org.infoUFPS.ValueObjects.RatingSummary;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GetMateriasPublicQuery {

    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    public GetMateriasPublicQuery() {
        this.materiaRepository = null;
        this.resenaRepository = null;
    }

    @Inject
    public GetMateriasPublicQuery(IMateriaRepository materiaRepository, IResenaRepository resenaRepository) {
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    public PageResult<PublicMateriaListItemDto> execute(String query, Integer creditos, TipoMateria tipo, PageRequest pageRequest) {
        MateriaSearchCriteria criteria = MateriaSearchCriteria.publicSearch(query, creditos, tipo);
        PageResult<Materia> result = materiaRepository.search(criteria, pageRequest);

        List<MateriaId> ids = result.items().stream().map(Materia::getId).toList();
        Map<String, RatingSummary> summaries = resenaRepository.getRatingSummariesByMateria(ids);

        List<PublicMateriaListItemDto> dtos = result.items().stream().map(m -> {
            RatingSummary summary = summaries.getOrDefault(m.getId().value(), RatingSummary.empty());
            return PublicMateriaListItemDto.of(m, summary.average(), summary.total());
        }).toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }
}
