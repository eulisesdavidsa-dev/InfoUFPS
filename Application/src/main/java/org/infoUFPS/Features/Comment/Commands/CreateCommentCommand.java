package org.infoUFPS.Features.Comment.Commands;

public record CreateCommentCommand(
        String contentId,
        String authorId,
        String text
) {
}

