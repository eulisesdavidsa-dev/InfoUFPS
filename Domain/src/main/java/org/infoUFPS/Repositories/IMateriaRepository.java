package org.infoUFPS.Repositories;

import org.infoUFPS.Criterias.MateriaSearchCriteria;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.MateriaId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IMateriaRepository {

    Materia save(Materia materia);

    Optional<Materia> findById(MateriaId id);

    Optional<Materia> findByCodigo(String codigo);

    PageResult<Materia> search(MateriaSearchCriteria criteria, PageRequest pageRequest);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndNotId(String codigo, MateriaId excludeId);

    List<Materia> findSimilar(String nombreNormalizado);

    List<Materia> findByProponenteId(AccountId proponenteId);

    long countTodaySubmissionsByProponente(AccountId proponenteId);

    List<Materia> findByIds(Collection<MateriaId> ids);

    void deleteById(MateriaId id);
}

