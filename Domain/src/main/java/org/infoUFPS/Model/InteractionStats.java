package org.infoUFPS.Model;

import org.infoUFPS.Exception.DomainException;

public class InteractionStats {

    private final int likesCount;
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

    public static InteractionStats empty() {
        return new InteractionStats(0, 0);
    }

    public static InteractionStats of(int likesCount, int commentsCount) {
        return new InteractionStats(likesCount, commentsCount);
    }

    public static InteractionStats onlyLikes(int likesCount) {
        return new InteractionStats(likesCount, 0);
    }

    public static InteractionStats onlyComments(int commentsCount) {
        return new InteractionStats(0, commentsCount);
    }

    public int likesCount() {return likesCount;}
    public int commentsCount() {return commentsCount;}

}
