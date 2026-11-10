package org.infoUFPS.Features.Profesor.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoUFPS.DTOs.MateriaSimpleDto;
import org.infoUFPS.DTOs.ProfesorDetailDto;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Entities.Profesor;
import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Exceptions.NotFoundException;
import org.infoUFPS.Exceptions.ResourceNotFoundException;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.Repositories.IProfesorRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.ValueObjects.MateriaId;
import org.infoUFPS.ValueObjects.ProfesorId;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class AssociateMateriasToProfesorHandler {

    private final IProfesorRepository profesorRepository;
    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    public AssociateMateriasToProfesorHandler() {
        this.profesorRepository = null;
        this.materiaRepository = null;
        this.resenaRepository = null;
    }

    @Inject
    public AssociateMateriasToProfesorHandler(IProfesorRepository profesorRepository,
                                             IMateriaRepository materiaRepository,
                                             IResenaRepository resenaRepository) {
        this.profesorRepository = profesorRepository;
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    @Transactional
    public ProfesorDetailDto execute(String profesorIdStr, Set<String> materiasIdsStr) {
        ProfesorId profesorId = ProfesorId.of(profesorIdStr);
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el profesor con ID: " + profesorIdStr));

        List<Materia> materias;
        if (materiasIdsStr != null) {
            Set<MateriaId> matIds = materiasIdsStr.stream().map(MateriaId::of).collect(Collectors.toSet());
            materias = validateMaterias(matIds);
            profesor.setMaterias(matIds);
        } else {
            materias = materiaRepository.findByIds(profesor.getMaterias());
        }

        profesorRepository.save(profesor);

        Double avg = resenaRepository.getAverageRatingForProfesor(profesorId);
        long count = resenaRepository.countPublishedByProfesor(profesorId);
        List<MateriaSimpleDto> materiaDtos = materias.stream().map(MateriaSimpleDto::from).toList();

        return ProfesorDetailDto.of(profesor, avg, count, materiaDtos, true);
    }

    private List<Materia> validateMaterias(Set<MateriaId> matIds) {
        List<Materia> materias = materiaRepository.findByIds(matIds);
        Set<String> encontradas = materias.stream()
                .map(materia -> materia.getId().value())
                .collect(Collectors.toSet());
        List<String> inexistentes = matIds.stream()
                .map(MateriaId::value)
                .filter(id -> !encontradas.contains(id))
                .sorted()
                .toList();
        if (!inexistentes.isEmpty()) {
            throw new NotFoundException("No se encontraron las materias: " + String.join(", ", inexistentes));
        }
        List<String> noPublicadas = materias.stream()
                .filter(materia -> materia.getEstado() != EstadoModeracion.PUBLICADO)
                .map(materia -> materia.getId().value())
                .sorted()
                .toList();
        if (!noPublicadas.isEmpty()) {
            throw new DomainException("Las siguientes materias no están publicadas: " + String.join(", ", noPublicadas));
        }
        return materias;
    }
}

