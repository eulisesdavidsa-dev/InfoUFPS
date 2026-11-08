package org.infoUFPS.Repositories;

import org.infoUFPS.Entities.Comment;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.CommentId;
import org.infoUFPS.ValueObjects.ContentId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto del repositorio para la gestión y persistencia de la entidad {@link Comment}.
 * Define el contrato que debe implementar cualquier mecanismo de persistencia (JPA, En Memoria, etc.).
 */
public interface ICommentRepository {

    /**
     * Guarda o actualiza un comentario en el almacenamiento.
     *
     * @param comment La entidad de dominio Comment a persistir.
     * @return El comentario persistido.
     */
    Comment save(Comment comment);

    /**
     * Busca un comentario por su identificador único.
     *
     * @param id El Value Object CommentId que identifica al comentario.
     * @return Un Optional con el Comment si fue encontrado, o vacío en caso contrario.
     */
    Optional<Comment> findById(CommentId id);

    /**
     * Obtiene todos los comentarios asociados a un contenido específico,
     * ordenados cronológicamente por su fecha de creación.
     *
     * @param contentId El identificador del contenido.
     * @return Lista de comentarios pertenecientes al contenido.
     */
    List<Comment> findByContentId(ContentId contentId);

    /**
     * Obtiene todos los comentarios realizados por una cuenta/usuario específico.
     *
     * @param authorId El identificador del autor.
     * @return Lista de comentarios realizados por el usuario.
     */
    List<Comment> findByAuthorId(AccountId authorId);

    /**
     * Obtiene todos los comentarios que un usuario en específico realizó dentro de un contenido concreto.
     * Satisface la regla donde un mismo usuario puede realizar múltiples comentarios en el mismo contenido.
     *
     * @param contentId El identificador del contenido.
     * @param authorId  El identificador del autor.
     * @return Lista de comentarios realizados por el autor en dicho contenido.
     */
    List<Comment> findByContentIdAndAuthorId(ContentId contentId, AccountId authorId);

    /**
     * Cuenta el total de comentarios asociados a un contenido.
     *
     * @param contentId El identificador del contenido.
     * @return Número total de comentarios registrados en el contenido.
     */
    long countByContentId(ContentId contentId);

    /**
     * Elimina físicamente un comentario de la base de datos a partir de su ID.
     *
     * @param id El CommentId del comentario a eliminar.
     */
    void deleteById(CommentId id);

    /**
     * Verifica la existencia de un comentario por su identificador único.
     *
     * @param id El CommentId a verificar.
     * @return true si el comentario existe, false de lo contrario.
     */
    boolean existsById(CommentId id);
}

