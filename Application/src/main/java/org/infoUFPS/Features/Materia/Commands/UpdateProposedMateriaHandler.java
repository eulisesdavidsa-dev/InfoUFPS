package org.infoUFPS.Features.Materia.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoUFPS.DTOs.MateriaDetailDto;
import org.infoUFPS.DTOs.Requests.UpdateProposedMateriaCommand;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Exceptions.DuplicateResourceException;
import org.infoUFPS.Exceptions.ForbiddenException;
import org.infoUFPS.Exceptions.ResourceNotFoundException;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.MateriaId;

@ApplicationScoped
public class UpdateProposedMateriaHandler {

    private final IMateriaRepository materiaRepository;

    public UpdateProposedMateriaHandler() {
        this.materiaRepository = null;
    }

    @Inject
    public UpdateProposedMateriaHandler(IMateriaRepository materiaRepository) {
        this.materiaRepository = materiaRepository;
    }

    @Transactional
    public MateriaDetailDto execute(UpdateProposedMateriaCommand command) {
        MateriaId materiaId = MateriaId.of(command.materiaId());
        AccountId editorId = AccountId.of(command.editorId());

        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la materia con ID: " + command.materiaId()));

        if (materia.getEstado() != EstadoModeracion.EN_REVISION) {
            throw new DomainException("Solo se pueden editar propuestas en revisión");
        }

        if (materia.getProponenteId() == null || !materia.getProponenteId().equals(editorId)) {
            throw new ForbiddenException("No tienes permiso para editar esta propuesta");
        }

        if (command.codigo() != null && !command.codigo().isBlank()) {
            String cleanCode = command.codigo().strip().toUpperCase();
            if (materiaRepository.existsByCodigoAndNotId(cleanCode, materiaId)) {
                throw new DuplicateResourceException("Ya existe una materia registrada con el código: " + cleanCode);
            }
        }

        materia.updateProposal(
                command.nombre(),
                command.codigo(),
                command.creditos(),
                command.tipo(),
                editorId
        );

        materiaRepository.save(materia);

        return MateriaDetailDto.of(materia, null, 0L);
    }
}

