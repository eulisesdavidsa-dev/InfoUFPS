package org.infoUFPS.Features.Moderacion.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.Criterias.ProfesorSearchCriteria;
import org.infoUFPS.DTOs.ProfesorDetailDto;
import org.infoUFPS.Entities.Profesor;
import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.Repositories.IProfesorRepository;
import org.infoUFPS.Repositories.IResenaRepository;

import java.util.List;

@ApplicationScoped
public class GetModeracionProfesoresQuery {

    private final IProfesorRepository profesorRepository;
    private final IResenaRepository resenaRepository;

    public GetModeracionProfesoresQuery() {
        this.profesorRepository = null;
        this.resenaRepository = null;
    }

    @Inject
    public GetModeracionProfesoresQuery(IProfesorRepository profesorRepository, IResenaRepository resenaRepository) {
        this.profesorRepository = profesorRepository;
        this.resenaRepository = resenaRepository;
    }

    public PageResult<ProfesorDetailDto> execute(EstadoModeracion estado, PageRequest pageRequest) {
        ProfesorSearchCriteria criteria = new ProfesorSearchCriteria(null, null, estado);
        PageResult<Profesor> result = profesorRepository.search(criteria, pageRequest);

        List<ProfesorDetailDto> dtos = result.items().stream().map(p -> {
            Double avg = resenaRepository.getAverageRatingForProfesor(p.getId());
            long total = resenaRepository.countPublishedByProfesor(p.getId());
            return ProfesorDetailDto.of(p, avg, total, List.of(), true);
        }).toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }
}

