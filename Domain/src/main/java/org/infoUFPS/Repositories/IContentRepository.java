package org.infoUFPS.Repositories;

import org.infoUFPS.Entities.Content;
import org.infoUFPS.Entities.Status;
import org.infoUFPS.ValueObjects.ContentId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto del repositorio para la gestión y persistencia de la entidad Content.
 * Define el contrato que debe implementar cualquier mecanismo de persistencia (ej. JPA, En Memoria).
 */
public interface IContentRepository {

    /**
     * Guarda o actualiza un contenido en la base de datos.
     * Si el contenido ya existe lo actualiza; si no, crea un nuevo registro.
     *
     * @param content La entidad de dominio Content a guardar.
     * @return El contenido persistido.
     */
    Content save(Content content);

    /**
     * Busca un contenido por su identificador único.
     *
     * @param id El Value Object ContentId que identifica al contenido.
     * @return Un Optional que contiene el Content si fue encontrado, o vacío si no existe.
     */
    Optional<Content> findById(ContentId id);

    /**
     * Obtiene únicamente los contenidos que se encuentran en estado PUBLISHED.
     * Diseñado para ser consumido por la interfaz pública de los usuarios finales.
     *
     * @return Lista de contenidos visibles públicamente.
     */
    List<Content> findAllPublished();

    /**
     * Obtiene una lista de contenidos filtrados por su estado interno del ciclo de vida.
     * Utilizado principalmente para paneles de administración y moderación.
     *
     * @param status El estado por el cual filtrar (UNDER_REVIEW, PUBLISHED, ARCHIVED).
     * @return Lista de contenidos que coinciden con el estado especificado.
     */
    List<Content> findByStatus(Status status);

    /**
     * Elimina físicamente un contenido de la base de datos a partir de su ID.
     *
     * @param id El ContentId del contenido a eliminar.
     */
    void deleteById(ContentId id);

    /**
     * Verifica la existencia de un contenido por su identificador único sin necesidad
     * de cargar toda la entidad a memoria.
     *
     * @param id El ContentId a verificar.
     * @return true si el contenido existe en la base de datos, false de lo contrario.
     */
    boolean existsById(ContentId id);

}
