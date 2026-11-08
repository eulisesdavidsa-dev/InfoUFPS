package org.infoUFPS.DTOs;

import java.time.LocalDateTime;

import org.infoUFPS.Entities.Comment;

public record CommentDto(
        String commentId,
        String contentId,
        String authorId,
        String text,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommentDto from(Comment comment) {
        return new CommentDto(
                comment.getId().value(),
                comment.getContentId().value(),
                comment.getAuthorId().value(),
                comment.getText(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}

