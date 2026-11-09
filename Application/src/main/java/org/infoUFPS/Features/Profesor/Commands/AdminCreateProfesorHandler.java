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
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.Repositories.IProfesorRepository;
import org.infoUFPS.Utils.TextNormalizer;
import org.infoUFPS.ValueObjects.Email;
import org.infoUFPS.ValueObjects.MateriaId;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AdminCreateProfesorHandler {

    private final IProfesorRepository profesorRepository;
    private final IMateriaRepository materiaRepository;

    public AdminCreateProfesorHandler() {
        this.profesorRepository = null;
        this.materiaRepository = null;
    }

    @Inject
    public AdminCreateProfesorHandler(IProfesorRepository profesorRepository, IMateriaRepository materiaRepository) {
        this.profesorRepository = profesorRepository;
        this.materiaRepository = materiaRepository;
    }

    @Transactional
    public ProfesorDetailDto execute(AdminSaveProfesorCommand command) {
        String emailNorm = Email.normalize(command.email());
        if (emailNorm != null && profesorRepository.existsByEmail(emailNorm)) {
            throw new DuplicateResourceException("Ya existe un profesor registrado con el email: " + emailNorm);
        }

        String normName = TextNormalizer.normalizeAndSortWords(command.nombre());
        if (profesorRepository.existsByNombreNormalizadoAndFacultadAndEstado(normName, command.facultad(), EstadoModeracion.PUBLICADO)) {
            throw new DuplicateResourceException("Ya existe un profesor publicado con este nombre en la facultad seleccionada");
        }

        Profesor profesor = Profesor.createByAdmin(command.nombre(), command.facultad(), emailNorm);

        List<MateriaSimpleDto> materiaDtos = new ArrayList<>();
        if (command.materiasIds() != null && !command.materiasIds().isEmpty()) {
            Set<MateriaId> matIds = command.materiasIds().stream().map(MateriaId::of).collect(Collectors.toSet());
            profesor.setMaterias(matIds);
            List<Materia> materias = materiaRepository.findByIds(matIds);
            materiaDtos = materias.stream().map(MateriaSimpleDto::from).toList();
        }

        profesorRepository.save(profesor);

        return ProfesorDetailDto.of(profesor, null, 0L, materiaDtos, true);
    }
}

