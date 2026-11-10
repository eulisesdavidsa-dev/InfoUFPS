package org.infoUFPS.Features.Search.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.Criterias.MateriaSearchCriteria;
import org.infoUFPS.Criterias.ProfesorSearchCriteria;
import org.infoUFPS.DTOs.GlobalSearchResultDto;
import org.infoUFPS.DTOs.ModuleItemDto;
import org.infoUFPS.DTOs.PublicMateriaListItemDto;
import org.infoUFPS.DTOs.PublicProfesorListItemDto;
import org.infoUFPS.Entities.Content;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Entities.Profesor;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.Repositories.IContentRepository;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.Repositories.IProfesorRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.Utils.TextNormalizer;
import org.infoUFPS.ValueObjects.MateriaId;
import org.infoUFPS.ValueObjects.ProfesorId;
import org.infoUFPS.ValueObjects.RatingSummary;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GlobalSearchQuery {

    private final IContentRepository contentRepository;
    private final IProfesorRepository profesorRepository;
    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    public GlobalSearchQuery() {
        this.contentRepository = null;
        this.profesorRepository = null;
        this.materiaRepository = null;
        this.resenaRepository = null;
    }

    @Inject
    public GlobalSearchQuery(IContentRepository contentRepository,
                             IProfesorRepository profesorRepository,
                             IMateriaRepository materiaRepository,
                             IResenaRepository resenaRepository) {
        this.contentRepository = contentRepository;
        this.profesorRepository = profesorRepository;
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    public GlobalSearchResultDto execute(String query) {
        String cleanQuery = (query != null) ? query.trim() : "";

        // 1. Contenidos publicados
        List<ModuleItemDto> contenidos = List.of();
        if (contentRepository != null) {
            contenidos = contentRepository.findAllPublished().stream()
                    .filter(c -> cleanQuery.isEmpty()
                            || TextNormalizer.matchesQueryTokens(c.getTitle(), cleanQuery)
                            || (c.getDescription() != null && TextNormalizer.matchesQueryTokens(c.getDescription(), cleanQuery)))
                    .limit(10)
                    .map(ModuleItemDto::from)
                    .toList();
        }

        // 2. Profesores publicados
        List<PublicProfesorListItemDto> profesores = List.of();
        if (profesorRepository != null) {
            PageResult<Profesor> profResult = profesorRepository.search(
                    ProfesorSearchCriteria.publicSearch(cleanQuery, null),
                    PageRequest.of(1, 10)
            );
            List<ProfesorId> ids = profResult.items().stream().map(Profesor::getId).toList();
            Map<String, RatingSummary> summaries = resenaRepository.getRatingSummariesByProfesor(ids);
            profesores = profResult.items().stream().map(p -> {
                RatingSummary summary = summaries.getOrDefault(p.getId().value(), RatingSummary.empty());
                return PublicProfesorListItemDto.of(p, summary.average(), summary.total());
            }).toList();
        }

        // 3. Materias publicadas
        List<PublicMateriaListItemDto> materias = List.of();
        if (materiaRepository != null) {
            PageResult<Materia> matResult = materiaRepository.search(
                    MateriaSearchCriteria.publicSearch(cleanQuery, null, null),
                    PageRequest.of(1, 10)
            );
            List<MateriaId> ids = matResult.items().stream().map(Materia::getId).toList();
            Map<String, RatingSummary> summaries = resenaRepository.getRatingSummariesByMateria(ids);
            materias = matResult.items().stream().map(m -> {
                RatingSummary summary = summaries.getOrDefault(m.getId().value(), RatingSummary.empty());
                return PublicMateriaListItemDto.of(m, summary.average(), summary.total());
            }).toList();
        }

        return new GlobalSearchResultDto(cleanQuery, contenidos, profesores, materias);
    }
}
