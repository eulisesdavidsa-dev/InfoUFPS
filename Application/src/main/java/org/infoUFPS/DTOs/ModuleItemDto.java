package org.infoUFPS.DTOs;

import org.infoUFPS.Entities.Content;
import java.util.Set;

public record ModuleItemDto(
        String contentId,
        String title,
        String subtitle,
        String description,
        String imageUrl,
        String categoryTag,
        Set<String> tags,
        String targetUrl,
        int likesCount,
        int commentsCount
) {
    // Factory method para convertir desde la Entidad de Dominio
    public static ModuleItemDto from(Content content) {
        return new ModuleItemDto(
                content.getId().value(),
                content.getTitle(),
                content.getSubtitle(),
                content.getDescription(),
                content.getImageUrl(),
                content.getCategoryTag(),
                content.getTags(),
                content.getTargetUrl(),
                content.getStats().getLikesCount(),
                content.getStats().getCommentsCount()
        );
    }
}
