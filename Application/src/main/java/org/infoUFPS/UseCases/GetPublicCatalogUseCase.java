package org.infoUFPS.UseCases;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.CatalogItemDto;
import org.infoUFPS.Repositories.IContentRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetPublicCatalogUseCase {

    private final IContentRepository repository;

    @Inject
    public GetPublicCatalogUseCase(IContentRepository repository) {
        this.repository = repository;
    }

    public Map<String, List<CatalogItemDto>> execute() {
        return repository.findAllPublished().stream()
                .map(CatalogItemDto::from)
                .collect(Collectors.groupingBy(
                        dto -> dto.categoryTag() != null ? dto.categoryTag() : "GENERAL",
                        LinkedHashMap::new,
                        Collectors.toList()));
    }
}
