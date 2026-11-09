package org.infoUFPS.Persistence.Repositories;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.infoUFPS.Entities.Comment;
import org.infoUFPS.Persistence.EntityConfigurations.CommentJpa;
import org.infoUFPS.Repositories.ICommentRepository;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.CommentId;
import org.infoUFPS.ValueObjects.ContentId;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de Persistencia JPA que implementa el contrato de Dominio {@link ICommentRepository}.
 * <p>
 * Gestiona el ciclo de vida de persistencia en la base de datos relacional para la entidad {@link Comment},
 * transformando los datos entre el Modelo de Dominio y la Entidad de Persistencia {@link CommentJpa}.
 */
@ApplicationScoped
public class CommentRepositoryJpa implements ICommentRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public Comment save(Comment comment) {
        CommentJpa entity = toEntity(comment);
        entityManager.merge(entity);
        return comment;
    }

    @Override
    public Optional<Comment> findById(CommentId id) {
        CommentJpa entity = entityManager.find(CommentJpa.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<Comment> findByContentId(ContentId contentId) {
        return entityManager.createQuery(
                        "SELECT c FROM CommentJpa c " +
                                "WHERE c.contentId = :contentId " +
                                "ORDER BY c.createdAt ASC",
                        CommentJpa.class)
                .setParameter("contentId", contentId.value())
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Comment> findByAuthorId(AccountId authorId) {
        return entityManager.createQuery(
                        "SELECT c FROM CommentJpa c " +
                                "WHERE c.authorId = :authorId " +
                                "ORDER BY c.createdAt DESC",
                        CommentJpa.class)
                .setParameter("authorId", authorId.value())
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Comment> findByContentIdAndAuthorId(ContentId contentId, AccountId authorId) {
        return entityManager.createQuery(
                        "SELECT c FROM CommentJpa c " +
                                "WHERE c.contentId = :contentId AND c.authorId = :authorId " +
                                "ORDER BY c.createdAt ASC",
                        CommentJpa.class)
                .setParameter("contentId", contentId.value())
                .setParameter("authorId", authorId.value())
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByContentId(ContentId contentId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(c) FROM CommentJpa c WHERE c.contentId = :contentId", Long.class)
                .setParameter("contentId", contentId.value())
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    @Transactional
    public void deleteById(CommentId id) {
        entityManager.createQuery(
                        "DELETE FROM CommentJpa c WHERE c.commentId = :id")
                .setParameter("id", id.value())
                .executeUpdate();
    }

    @Override
    public boolean existsById(CommentId id) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(c) FROM CommentJpa c WHERE c.commentId = :id", Long.class)
                .setParameter("id", id.value())
                .getSingleResult();
        return count != null && count > 0;
    }

    // ── Mapeo: Dominio → JPA Entity ──────────────────────────────────

    private CommentJpa toEntity(Comment domain) {
        CommentJpa entity = new CommentJpa();
        entity.setCommentId(domain.getId().value());
        entity.setContentId(domain.getContentId().value());
        entity.setAuthorId(domain.getAuthorId().value());
        entity.setText(domain.getText());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    // ── Mapeo: JPA Entity → Dominio ──────────────────────────────────

    private Comment toDomain(CommentJpa jpa) {
        return Comment.of(
                CommentId.of(jpa.getCommentId()),
                ContentId.of(jpa.getContentId()),
                AccountId.of(jpa.getAuthorId()),
                jpa.getText(),
                jpa.getCreatedAt(),
                jpa.getUpdatedAt()
        );
    }
}

