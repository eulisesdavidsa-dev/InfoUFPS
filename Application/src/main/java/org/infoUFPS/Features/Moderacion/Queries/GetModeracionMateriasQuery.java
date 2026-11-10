package org.infoUFPS.Features.Moderacion.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.Criterias.MateriaSearchCriteria;
import org.infoUFPS.DTOs.MateriaDetailDto;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.ValueObjects.MateriaId;
import org.infoUFPS.ValueObjects.RatingSummary;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GetModeracionMateriasQuery {

    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    public GetModeracionMateriasQuery() {
        this.materiaRepository = null;
        this.resenaRepository = null;
    }

    @Inject
    public GetModeracionMateriasQuery(IMateriaRepository materiaRepository, IResenaRepository resenaRepository) {
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    public PageResult<MateriaDetailDto> execute(EstadoModeracion estado, PageRequest pageRequest) {
        MateriaSearchCriteria criteria = new MateriaSearchCriteria(null, null, null, estado);
        PageResult<Materia> result = materiaRepository.search(criteria, pageRequest);

        List<MateriaId> ids = result.items().stream().map(Materia::getId).toList();
        Map<String, RatingSummary> summaries = resenaRepository.getRatingSummariesByMateria(ids);

        List<MateriaDetailDto> dtos = result.items().stream().map(m -> {
            RatingSummary summary = summaries.getOrDefault(m.getId().value(), RatingSummary.empty());
            return MateriaDetailDto.of(m, summary.average(), summary.total());
        }).toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }
}
