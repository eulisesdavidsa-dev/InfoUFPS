package org.infoUFPS.Features.Profesor.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.MateriaSimpleDto;
import org.infoUFPS.DTOs.PublicProfesorDetailDto;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Entities.Profesor;
import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Exceptions.ResourceNotFoundException;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.Repositories.IProfesorRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.ProfesorId;

import java.util.List;

@ApplicationScoped
public class GetProfesorDetailQuery {

    private final IProfesorRepository profesorRepository;
    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    public GetProfesorDetailQuery() {
        this.profesorRepository = null;
        this.materiaRepository = null;
        this.resenaRepository = null;
    }

    @Inject
    public GetProfesorDetailQuery(IProfesorRepository profesorRepository,
                                  IMateriaRepository materiaRepository,
                                  IResenaRepository resenaRepository) {
        this.profesorRepository = profesorRepository;
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    public PublicProfesorDetailDto execute(String profesorIdStr, AccountId requesterId, boolean isAdmin) {
        ProfesorId profesorId = ProfesorId.of(profesorIdStr);
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el profesor con ID: " + profesorIdStr));

        boolean isAuthor = requesterId != null && requesterId.equals(profesor.getProponenteId());

        if (profesor.getEstado() != EstadoModeracion.PUBLICADO && !isAdmin && !isAuthor) {
            throw new ResourceNotFoundException("El profesor no está disponible o no ha sido publicado");
        }

        Double avg = resenaRepository.getAverageRatingForProfesor(profesorId);
        long total = resenaRepository.countPublishedByProfesor(profesorId);
        List<Materia> materias = materiaRepository.findByIds(profesor.getMaterias());
        List<MateriaSimpleDto> materiaDtos = materias.stream().map(MateriaSimpleDto::from).toList();

        return PublicProfesorDetailDto.of(profesor, avg, total, materiaDtos);
    }
}
