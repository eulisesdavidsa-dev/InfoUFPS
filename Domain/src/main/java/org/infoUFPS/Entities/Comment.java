package org.infoUFPS.Entities;

import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.CommentId;
import org.infoUFPS.ValueObjects.ContentId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de Dominio que representa un comentario realizado sobre un contenido.
 * <p>
 * Reglas de negocio del modelo:
 * - Un contenido puede albergar múltiples comentarios (1 Content -> N Comments).
 * - Cada comentario es realizado por un usuario (Account) específico (1 Account -> N Comments).
 * - Un mismo usuario puede realizar múltiples comentarios dentro del mismo contenido.
 * - El texto del comentario no puede ser nulo, vacío ni exceder el límite establecido (1000 caracteres).
 */
public class Comment {

    /** Identificador único e inmutable del comentario. */
    private final CommentId id;

    /** Identificador del contenido al que pertenece el comentario. */
    private final ContentId contentId;

    /** Identificador de la cuenta/usuario que realizó el comentario. */
    private final AccountId authorId;

    /** Texto del comentario. */
    private String text;

    /** Fecha y hora en la que fue publicado el comentario. */
    private final LocalDateTime createdAt;

    /** Fecha y hora de la última modificación (si ha sido editado). */
    private LocalDateTime updatedAt;

    /** Longitud máxima permitida para el texto del comentario. */
    public static final int MAX_TEXT_LENGTH = 1000;

    /**
     * Constructor privado accesible solo mediante métodos de fábrica estáticos.
     */
    private Comment(CommentId id,
                    ContentId contentId,
                    AccountId authorId,
                    String text,
                    LocalDateTime createdAt,
                    LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "El id del comentario es obligatorio");
        this.contentId = Objects.requireNonNull(contentId, "El contentId es obligatorio");
        this.authorId = Objects.requireNonNull(authorId, "El authorId es obligatorio");
        this.text = validateAndSanitizeText(text);
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
        this.updatedAt = updatedAt;
    }

    /**
     * Fábrica para crear un nuevo comentario de dominio.
     * Genera automáticamente un nuevo {@link CommentId} y asigna la fecha de creación actual.
     *
     * @param contentId Identificador del contenido que se comenta.
     * @param authorId  Identificador del usuario autor del comentario.
     * @param text      Cuerpo del comentario.
     * @return Nueva instancia de Comment válida.
     */
    public static Comment create(ContentId contentId, AccountId authorId, String text) {
        return new Comment(
                CommentId.generate(),
                contentId,
                authorId,
                text,
                LocalDateTime.now(),
                null
        );
    }

    /**
     * Fábrica para reconstruir un comentario desde la capa de persistencia (repositorios).
     */
    public static Comment of(CommentId id,
                             ContentId contentId,
                             AccountId authorId,
                             String text,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
        return new Comment(id, contentId, authorId, text, createdAt, updatedAt);
    }

    /**
     * Regla de negocio: Actualiza el texto del comentario y registra la marca de tiempo de modificación.
     *
     * @param newText El nuevo texto a asignar.
     */
    public void updateText(String newText) {
        this.text = validateAndSanitizeText(newText);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Valida y sanitiza el texto ingresado.
     */
    private static String validateAndSanitizeText(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new DomainException("El texto del comentario no puede estar vacío");
        }
        String sanitized = rawText.strip();
        if (sanitized.length() > MAX_TEXT_LENGTH) {
            throw new DomainException("El texto del comentario no puede exceder los " + MAX_TEXT_LENGTH + " caracteres");
        }
        return sanitized;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public CommentId getId() { return id; }
    public ContentId getContentId() { return contentId; }
    public AccountId getAuthorId() { return authorId; }
    public String getText() { return text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

