package org.infoUFPS.Repositories;

import org.infoUFPS.Entities.Resena;
import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Enums.TipoDestino;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.MateriaId;
import org.infoUFPS.ValueObjects.ProfesorId;
import org.infoUFPS.ValueObjects.ResenaId;

import java.util.List;
import java.util.Optional;

public interface IResenaRepository {

    Resena save(Resena resena);

    Optional<Resena> findById(ResenaId id);

    Optional<Resena> findActiveByUserAndProfesor(AccountId autorId, ProfesorId profesorId);

    Optional<Resena> findActiveByUserAndMateria(AccountId autorId, MateriaId materiaId);

    PageResult<Resena> findPublishedByProfesor(ProfesorId profesorId, PageRequest pageRequest);

    PageResult<Resena> findPublishedByMateria(MateriaId materiaId, PageRequest pageRequest);

    List<Resena> findByAutorId(AccountId autorId);

    PageResult<Resena> searchModeration(TipoDestino destino, EstadoModeracion estado, PageRequest pageRequest);

    Double getAverageRatingForProfesor(ProfesorId profesorId);

    long countPublishedByProfesor(ProfesorId profesorId);

    Double getAverageRatingForMateria(MateriaId materiaId);

    long countPublishedByMateria(MateriaId materiaId);

    long countTodaySubmissionsByAutor(AccountId autorId);

    void deleteById(ResenaId id);
}

