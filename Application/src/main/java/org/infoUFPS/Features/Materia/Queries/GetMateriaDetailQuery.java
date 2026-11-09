package org.infoUFPS.Features.Materia.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.MateriaDetailDto;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Exceptions.ResourceNotFoundException;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.MateriaId;

@ApplicationScoped
public class GetMateriaDetailQuery {

    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    public GetMateriaDetailQuery() {
        this.materiaRepository = null;
        this.resenaRepository = null;
    }

    @Inject
    public GetMateriaDetailQuery(IMateriaRepository materiaRepository, IResenaRepository resenaRepository) {
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    public MateriaDetailDto execute(String materiaIdStr, AccountId requesterId, boolean isAdmin) {
        MateriaId materiaId = MateriaId.of(materiaIdStr);
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la materia con ID: " + materiaIdStr));

        boolean isAuthor = requesterId != null && requesterId.equals(materia.getProponenteId());

        if (materia.getEstado() != EstadoModeracion.PUBLICADO && !isAdmin && !isAuthor) {
            throw new ResourceNotFoundException("La materia no está disponible o no ha sido publicada");
        }

        Double avg = resenaRepository.getAverageRatingForMateria(materiaId);
        long total = resenaRepository.countPublishedByMateria(materiaId);

        return MateriaDetailDto.of(materia, avg, total);
    }
}

