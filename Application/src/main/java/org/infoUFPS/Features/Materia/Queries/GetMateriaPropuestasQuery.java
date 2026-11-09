package org.infoUFPS.Features.Materia.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.MateriaDetailDto;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.ValueObjects.AccountId;

import java.util.List;

@ApplicationScoped
public class GetMateriaPropuestasQuery {

    private final IMateriaRepository materiaRepository;

    public GetMateriaPropuestasQuery() {
        this.materiaRepository = null;
    }

    @Inject
    public GetMateriaPropuestasQuery(IMateriaRepository materiaRepository) {
        this.materiaRepository = materiaRepository;
    }

    public List<MateriaDetailDto> execute(String proponenteIdStr) {
        AccountId proponenteId = AccountId.of(proponenteIdStr);
        List<Materia> materias = materiaRepository.findByProponenteId(proponenteId);
        return materias.stream()
                .map(m -> MateriaDetailDto.of(m, null, 0L))
                .toList();
    }
}

