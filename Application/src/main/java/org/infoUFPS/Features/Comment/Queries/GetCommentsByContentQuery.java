package org.infoUFPS.Features.Comment.Queries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.CommentDto;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Repositories.ICommentRepository;
import org.infoUFPS.ValueObjects.ContentId;

import java.util.List;

@ApplicationScoped
public class GetCommentsByContentQuery {

    private final ICommentRepository commentRepository;

    public GetCommentsByContentQuery() {
        this.commentRepository = null;
    }

    @Inject
    public GetCommentsByContentQuery(ICommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Obtiene todos los comentarios asociados a un contenido específico.
     *
     * @param contentId Identificador del contenido.
     * @return Lista de DTOs de comentarios ordenados cronológicamente.
     */
    public List<CommentDto> execute(String contentId) {
        if (contentId == null || contentId.isBlank()) {
            throw new DomainException("El contentId es obligatorio");
        }
        return commentRepository.findByContentId(ContentId.of(contentId)).stream()
                .map(CommentDto::from)
                .toList();
    }
}

