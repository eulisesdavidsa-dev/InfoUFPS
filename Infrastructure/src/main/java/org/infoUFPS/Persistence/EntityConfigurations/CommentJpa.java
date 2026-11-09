package org.infoUFPS.Persistence.EntityConfigurations;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad de Persistencia (JPA/Hibernate) que mapea el modelo relacional para la tabla 'comments'.
 * <p>
 * Representa la tabla de base de datos para almacenar los comentarios de los contenidos.
 * Implementa la relación donde un contenido tiene múltiples comentarios (1 Content -> N Comments),
 * un usuario realiza comentarios (1 Account -> N Comments), y un usuario puede realizar múltiples
 * comentarios dentro del mismo contenido (sin índice único en content_id + author_id).
 */
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_content_id", columnList = "content_id"),
        @Index(name = "idx_comments_author_id", columnList = "author_id"),
        @Index(name = "idx_comments_content_author", columnList = "content_id, author_id"),
        @Index(name = "idx_comments_created_at", columnList = "created_at")
})
public class CommentJpa {

    /** Identificador primario de la tabla (UUID de 36 caracteres en formato String). */
    @Id
    @Column(name = "comment_id", length = 36, nullable = false)
    private String commentId;

    /** Referencia al identificador del contenido comentado. */
    @Column(name = "content_id", length = 36, nullable = false)
    private String contentId;

    /** Referencia al identificador de la cuenta o usuario que realizó el comentario. */
    @Column(name = "author_id", length = 36, nullable = false)
    private String authorId;

    /** Texto del comentario. */
    @Column(name = "text", columnDefinition = "TEXT", nullable = false)
    private String text;

    /** Fecha y hora de creación del comentario. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Fecha y hora de la última modificación. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Constructor por defecto sin argumentos requerido por la especificación JPA/Hibernate.
     */
    public CommentJpa() {
    }

    // ── Getters y Setters ────────────────────────────────────────────────

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

