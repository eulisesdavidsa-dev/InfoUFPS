package org.infoUFPS.ValueObjects;

import org.infoUFPS.Exceptions.DomainException;

/**
 * Objeto de Valor (Value Object) inmutable que encapsula las métricas de interacción
 * (número de me gusta y comentarios) asociadas a la vista previa de un contenido.
 * Garantiza que las métricas nunca adquieran valores negativos y se actualiza
 * mediante reemplazo de instancias.
 */
public final class InteractionStats {

    /** Cantidad total de likes a mostrar en la tarjeta. */
    private final int likesCount;

    /** Cantidad total de comentarios a mostrar en la tarjeta. */
    private final int commentsCount;

    private InteractionStats(int likesCount, int commentsCount) {
        if (likesCount < 0) {
            throw new DomainException("El contador de likes no puede ser negativo.");
        }
        if (commentsCount < 0) {
            throw new DomainException("El contador de comentarios no puede ser negativo.");
        }
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
    }

    /**
     * Inicializa los contadores en cero para una nueva vista previa de contenido.
     */
    public static InteractionStats empty() {
        return new InteractionStats(0, 0);
    }

    /**
     * Instancia las métricas con los contadores actuales obtenidos de la persistencia.
     */
    public static InteractionStats of(int likesCount, int commentsCount) {
        return new InteractionStats(likesCount, commentsCount);
    }

    public static InteractionStats onlyLikes(int likesCount) {
        return new InteractionStats(likesCount, 0);
    }

    public static InteractionStats onlyComments(int commentsCount) {
        return new InteractionStats(0, commentsCount);
    }

    // ── Métodos para actualizar el contador de la tarjeta ─────────────

    /**
     * Devuelve una nueva instancia de estadísticas incrementando en 1 el contador de likes.
     */
    public InteractionStats incrementarLikes() {
        return new InteractionStats(this.likesCount + 1, this.commentsCount);
    }

    /**
     * Devuelve una nueva instancia de estadísticas incrementando en 1 el contador de comentarios.
     */
    public InteractionStats incrementarComentarios() {
        return new InteractionStats(this.likesCount, this.commentsCount + 1);
    }

    // ── Getters para la vista ─────────────────────────────────────────

    public int getLikesCount() { return likesCount; }
    public int getCommentsCount() { return commentsCount; }
}
