package org.infoUFPS.Repositories;

import org.infoUFPS.Criterias.ProfesorSearchCriteria;
import org.infoUFPS.Entities.Profesor;
import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Enums.Facultad;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.ProfesorId;

import java.util.List;
import java.util.Optional;

public interface IProfesorRepository {

    Profesor save(Profesor profesor);

    Optional<Profesor> findById(ProfesorId id);

    PageResult<Profesor> search(ProfesorSearchCriteria criteria, PageRequest pageRequest);

    boolean existsByNombreNormalizadoAndFacultadAndEstado(String nombreNormalizado, Facultad facultad, EstadoModeracion estado);

    boolean existsByEmail(String email);

    boolean existsByEmailAndNotId(String email, ProfesorId excludeId);

    List<Profesor> findSimilar(String nombreNormalizado, Facultad facultad);

    List<Profesor> findByProponenteId(AccountId proponenteId);

    long countTodaySubmissionsByProponente(AccountId proponenteId);

    void deleteById(ProfesorId id);
}

