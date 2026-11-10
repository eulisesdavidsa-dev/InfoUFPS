package org.infoUFPS.Features.Profesor.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoUFPS.DTOs.ProfesorDetailDto;
import org.infoUFPS.DTOs.Requests.UpdateProposedProfesorCommand;
import org.infoUFPS.Entities.Profesor;
import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Enums.Facultad;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Exceptions.DuplicateResourceException;
import org.infoUFPS.Exceptions.ForbiddenException;
import org.infoUFPS.Exceptions.ResourceNotFoundException;
import org.infoUFPS.Repositories.IProfesorRepository;
import org.infoUFPS.Utils.TextNormalizer;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.Email;
import org.infoUFPS.ValueObjects.ProfesorId;

import java.util.List;

@ApplicationScoped
public class UpdateProposedProfesorHandler {

    private final IProfesorRepository profesorRepository;

    public UpdateProposedProfesorHandler() {
        this.profesorRepository = null;
    }

    @Inject
    public UpdateProposedProfesorHandler(IProfesorRepository profesorRepository) {
        this.profesorRepository = profesorRepository;
    }

    @Transactional
    public ProfesorDetailDto execute(UpdateProposedProfesorCommand command) {
        ProfesorId profesorId = ProfesorId.of(command.profesorId());
        AccountId editorId = AccountId.of(command.editorId());

        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el profesor con ID: " + command.profesorId()));

        if (profesor.getEstado() != EstadoModeracion.EN_REVISION) {
            throw new DomainException("Solo se pueden editar propuestas en estado de revisión");
        }

        if (profesor.getProponenteId() == null || !profesor.getProponenteId().equals(editorId)) {
            throw new ForbiddenException("No tienes permiso para editar esta propuesta");
        }

        Facultad facultadEfectiva = command.facultad() != null
                ? command.facultad()
                : (profesor.getFacultad() != null ? profesor.getFacultad() : Facultad.NO_ESPECIFICADA);

        String emailNorm = Email.normalize(command.email());
        if (emailNorm != null && profesorRepository.existsByEmailAndNotId(emailNorm, profesorId)) {
            throw new DuplicateResourceException("Ya existe un profesor registrado con el email: " + emailNorm);
        }

        String normName = TextNormalizer.normalizeAndSortWords(command.nombre());
        if (!normName.equals(profesor.getNombreNormalizado()) || facultadEfectiva != profesor.getFacultad()) {
            if (profesorRepository.existsByNombreNormalizadoAndFacultadAndEstado(normName, facultadEfectiva, EstadoModeracion.PUBLICADO)) {
                throw new DuplicateResourceException("Ya existe un profesor publicado con este nombre en la facultad seleccionada");
            }
        }

        profesor.updateProposal(command.nombre(), command.facultad(), emailNorm, editorId);
        profesorRepository.save(profesor);

        return ProfesorDetailDto.of(profesor, null, 0L, List.of(), false);
    }
}
