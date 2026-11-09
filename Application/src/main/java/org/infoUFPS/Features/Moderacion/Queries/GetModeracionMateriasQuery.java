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

import java.util.List;

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

        List<MateriaDetailDto> dtos = result.items().stream().map(m -> {
            Double avg = resenaRepository.getAverageRatingForMateria(m.getId());
            long total = resenaRepository.countPublishedByMateria(m.getId());
            return MateriaDetailDto.of(m, avg, total);
        }).toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }
}

