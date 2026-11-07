package org.infoUFPS.Features.Content.Queries;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.ModuleItemDto;
import org.infoUFPS.Repositories.IContentRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetModuleQuery {

    private final IContentRepository repository;

    @Inject
    public GetModuleQuery(IContentRepository repository) {
        this.repository = repository;
    }

    public Map<String, List<ModuleItemDto>> execute() {
        return repository.findAllPublished().stream()
                .map(ModuleItemDto::from)
                .collect(Collectors.groupingBy(
                        dto -> dto.categoryTag() != null ? dto.categoryTag() : "GENERAL",
                        LinkedHashMap::new,
                        Collectors.toList()));
    }
}
