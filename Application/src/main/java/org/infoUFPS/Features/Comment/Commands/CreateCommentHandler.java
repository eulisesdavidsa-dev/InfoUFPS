package org.infoUFPS.Features.Comment.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.CommentDto;
import org.infoUFPS.Entities.Comment;
import org.infoUFPS.Entities.Content;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Repositories.IAccountRepository;
import org.infoUFPS.Repositories.ICommentRepository;
import org.infoUFPS.Repositories.IContentRepository;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.ContentId;

@ApplicationScoped
public class CreateCommentHandler {

    private final ICommentRepository commentRepository;
    private final IContentRepository contentRepository;
    private final IAccountRepository accountRepository;

    public CreateCommentHandler() {
        this.commentRepository = null;
        this.contentRepository = null;
        this.accountRepository = null;
    }

    @Inject
    public CreateCommentHandler(ICommentRepository commentRepository,
                                IContentRepository contentRepository,
                                IAccountRepository accountRepository) {
        this.commentRepository = commentRepository;
        this.contentRepository = contentRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Ejecuta el caso de uso para registrar un comentario:
     * 1. Valida la existencia del contenido.
     * 2. Valida la existencia de la cuenta / usuario autor.
     * 3. Satisface la regla de negocio: un mismo usuario puede realizar varios comentarios en el mismo contenido.
     * 4. Crea y persiste la entidad Comment.
     * 5. Incrementa el contador de comentarios en la tarjeta de Content.
     *
     * @param command Datos de entrada para crear el comentario.
     * @return DTO con la información del comentario registrado.
     */
    public CommentDto execute(CreateCommentCommand command) {
        if (command.contentId() == null || command.contentId().isBlank()) {
            throw new DomainException("El contentId es obligatorio");
        }
        if (command.authorId() == null || command.authorId().isBlank()) {
            throw new DomainException("El authorId es obligatorio");
        }

        ContentId contentId = ContentId.of(command.contentId());
        AccountId authorId = AccountId.of(command.authorId());

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new DomainException("El contenido especificado no existe: " + command.contentId()));

        if (accountRepository.findById(authorId).isEmpty()) {
            throw new DomainException("El autor especificado no existe: " + command.authorId());
        }

        Comment comment = Comment.create(contentId, authorId, command.text());
        commentRepository.save(comment);

        // Actualizar estadísticas del contenido
        content.incrementComments();
        contentRepository.save(content);

        return CommentDto.from(comment);
    }
}

