package org.infoUFPS.Features.Materia.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.Criterias.MateriaSearchCriteria;
import org.infoUFPS.DTOs.MateriaListItemDto;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Enums.TipoMateria;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.Repositories.IResenaRepository;

import java.util.List;

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

    public PageResult<MateriaListItemDto> execute(String query, Integer creditos, TipoMateria tipo, PageRequest pageRequest) {
        MateriaSearchCriteria criteria = MateriaSearchCriteria.publicSearch(query, creditos, tipo);
        PageResult<Materia> result = materiaRepository.search(criteria, pageRequest);

        List<MateriaListItemDto> dtos = result.items().stream().map(m -> {
            Double avg = resenaRepository.getAverageRatingForMateria(m.getId());
            long total = resenaRepository.countPublishedByMateria(m.getId());
            return MateriaListItemDto.of(m, avg, total);
        }).toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }
}

