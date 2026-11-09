package org.infoUFPS.Features.Profesor.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoUFPS.DTOs.MateriaSimpleDto;
import org.infoUFPS.DTOs.ProfesorDetailDto;
import org.infoUFPS.DTOs.Requests.AdminSaveProfesorCommand;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Entities.Profesor;
import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Exceptions.DuplicateResourceException;
import org.infoUFPS.Exceptions.ResourceNotFoundException;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.Repositories.IProfesorRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.Utils.TextNormalizer;
import org.infoUFPS.ValueObjects.Email;
import org.infoUFPS.ValueObjects.MateriaId;
import org.infoUFPS.ValueObjects.ProfesorId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class AdminUpdateProfesorHandler {

    private final IProfesorRepository profesorRepository;
    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    public AdminUpdateProfesorHandler() {
        this.profesorRepository = null;
        this.materiaRepository = null;
        this.resenaRepository = null;
    }

    @Inject
    public AdminUpdateProfesorHandler(IProfesorRepository profesorRepository,
                                      IMateriaRepository materiaRepository,
                                      IResenaRepository resenaRepository) {
        this.profesorRepository = profesorRepository;
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    @Transactional
    public ProfesorDetailDto execute(String id, AdminSaveProfesorCommand command) {
        ProfesorId profesorId = ProfesorId.of(id);
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el profesor con ID: " + id));

        String emailNorm = Email.normalize(command.email());
        if (emailNorm != null && profesorRepository.existsByEmailAndNotId(emailNorm, profesorId)) {
            throw new DuplicateResourceException("Ya existe un profesor registrado con el email: " + emailNorm);
        }

        String normName = TextNormalizer.normalizeAndSortWords(command.nombre());
        if (!normName.equals(profesor.getNombreNormalizado()) || command.facultad() != profesor.getFacultad()) {
            if (profesorRepository.existsByNombreNormalizadoAndFacultadAndEstado(normName, command.facultad(), EstadoModeracion.PUBLICADO)) {
                throw new DuplicateResourceException("Ya existe un profesor publicado con este nombre en la facultad seleccionada");
            }
        }

        profesor.adminUpdate(command.nombre(), command.facultad(), emailNorm);

        if (command.materiasIds() != null) {
            Set<MateriaId> matIds = command.materiasIds().stream().map(MateriaId::of).collect(Collectors.toSet());
            profesor.setMaterias(matIds);
        }

        profesorRepository.save(profesor);

        Double avg = resenaRepository.getAverageRatingForProfesor(profesorId);
        long count = resenaRepository.countPublishedByProfesor(profesorId);
        List<Materia> materias = materiaRepository.findByIds(profesor.getMaterias());
        List<MateriaSimpleDto> materiaDtos = materias.stream().map(MateriaSimpleDto::from).toList();

        return ProfesorDetailDto.of(profesor, avg, count, materiaDtos, true);
    }
}

