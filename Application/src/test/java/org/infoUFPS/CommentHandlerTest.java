package org.infoUFPS;

import org.infoUFPS.DTOs.CommentDto;
import org.infoUFPS.Entities.Comment;
import org.infoUFPS.Entities.Content;
import org.infoUFPS.Enums.ModuleType;
import org.infoUFPS.Enums.Status;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Features.Comment.Commands.CreateCommentCommand;
import org.infoUFPS.Features.Comment.Commands.CreateCommentHandler;
import org.infoUFPS.Repositories.ICommentRepository;
import org.infoUFPS.Repositories.IContentRepository;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.CommentId;
import org.infoUFPS.ValueObjects.ContentId;
import org.infoUFPS.ValueObjects.InteractionStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CreateCommentHandler: contenidos publicados y contador atómico")
class CommentHandlerTest {

    private InMemoryContentRepository contentRepository;
    private InMemoryCommentRepository commentRepository;
    private ApplicationUseCasesTest.InMemoryAccountRepository accountRepository;
    private CreateCommentHandler handler;

    private AccountId author;
    private Content published;
    private Content underReview;

    @BeforeEach
    void setUp() {
        contentRepository = new InMemoryContentRepository();
        commentRepository = new InMemoryCommentRepository();
        accountRepository = new ApplicationUseCasesTest.InMemoryAccountRepository();

        author = accountRepository.save(org.infoUFPS.Entities.Account.register(
                "comentador@ufps.edu.co", "hash", "Comentador")).getAccountId();

        published = content(Status.PUBLISHED);
        underReview = content(Status.UNDER_REVIEW);
        contentRepository.save(published);
        contentRepository.save(underReview);

        handler = new CreateCommentHandler(commentRepository, contentRepository, accountRepository);
    }

    private Content content(Status status) {
        return Content.create("Contenido", "Subtítulo", "Descripción", null, "ACADEMICO",
                Set.of(), "https://info.ufps.edu.co", InteractionStats.empty(), status,
                ModuleType.LA_VITRINA);
    }

    @Test
    @DisplayName("Permite comentar un contenido publicado e incrementa el contador")
    void createsCommentOnPublishedContent() {
        CommentDto dto = handler.execute(new CreateCommentCommand(
                published.getId().value(), author.value(), "Buen aporte"));

        assertEquals("Buen aporte", dto.text());
        assertEquals(1, commentRepository.countByContentId(published.getId()));
        assertEquals(1, contentRepository.findById(published.getId()).orElseThrow()
                .getStats().getCommentsCount());
    }

    @Test
    @DisplayName("Rechaza comentar un contenido no publicado sin tocar el contador")
    void rejectsCommentOnUnpublishedContent() {
        assertThrows(DomainException.class, () -> handler.execute(new CreateCommentCommand(
                underReview.getId().value(), author.value(), "Comentario")));

        assertEquals(0, commentRepository.countByContentId(underReview.getId()));
        assertEquals(0, contentRepository.findById(underReview.getId()).orElseThrow()
                .getStats().getCommentsCount());
    }

    @Test
    @DisplayName("Rechaza comentar con un autor inexistente")
    void rejectsUnknownAuthor() {
        assertThrows(DomainException.class, () -> handler.execute(new CreateCommentCommand(
                published.getId().value(), AccountId.generate().value(), "Comentario")));

        assertEquals(0, commentRepository.countByContentId(published.getId()));
    }

    static class InMemoryContentRepository implements IContentRepository {
        private final Map<String, Content> db = new LinkedHashMap<>();

        @Override public Content save(Content content) { db.put(content.getId().value(), content); return content; }
        @Override public Optional<Content> findById(ContentId id) { return Optional.ofNullable(db.get(id.value())); }
        @Override public List<Content> findAllPublished() {
            return db.values().stream().filter(c -> c.getStatus() == Status.PUBLISHED).toList();
        }
        @Override public List<Content> findByStatus(Status status) {
            return db.values().stream().filter(c -> c.getStatus() == status).toList();
        }
        @Override public List<Content> findPublishedByModuleType(ModuleType moduleType) {
            return db.values().stream()
                    .filter(c -> c.getStatus() == Status.PUBLISHED && c.getModuleType() == moduleType)
                    .toList();
        }
        @Override public void deleteById(ContentId id) { db.remove(id.value()); }
        @Override public boolean existsById(ContentId id) { return db.containsKey(id.value()); }
        @Override public void incrementCommentsCount(ContentId id) {
            Content content = db.get(id.value());
            if (content != null) {
                content.incrementComments();
            }
        }
        @Override public void decrementCommentsCount(ContentId id) {
            Content content = db.get(id.value());
            if (content != null) {
                content.decrementComments();
            }
        }
    }

    static class InMemoryCommentRepository implements ICommentRepository {
        private final Map<String, Comment> db = new LinkedHashMap<>();

        @Override public Comment save(Comment comment) { db.put(comment.getId().value(), comment); return comment; }
        @Override public Optional<Comment> findById(CommentId id) { return Optional.ofNullable(db.get(id.value())); }
        @Override public List<Comment> findByContentId(ContentId contentId) {
            return db.values().stream().filter(c -> contentId.equals(c.getContentId())).toList();
        }
        @Override public List<Comment> findByAuthorId(AccountId authorId) {
            return db.values().stream().filter(c -> authorId.equals(c.getAuthorId())).toList();
        }
        @Override public List<Comment> findByContentIdAndAuthorId(ContentId contentId, AccountId authorId) {
            return db.values().stream()
                    .filter(c -> contentId.equals(c.getContentId()) && authorId.equals(c.getAuthorId()))
                    .toList();
        }
        @Override public long countByContentId(ContentId contentId) {
            return db.values().stream().filter(c -> contentId.equals(c.getContentId())).count();
        }
        @Override public void deleteById(CommentId id) { db.remove(id.value()); }
        @Override public boolean existsById(CommentId id) { return db.containsKey(id.value()); }
    }
}
