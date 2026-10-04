package org.infoUFPS.Model;

import org.infoUFPS.Exception.DomainException;

import java.time.LocalDateTime;
import java.util.*;

public class Content {

    private ContentId id;
    private String title;
    private String subtitle;
    private String description;
    private String imageUrl;              // Foto o Banner
    private String categoryTag;           // // Ej: "ACADÉMICO", "ARRIENDOS", "PROYECTO" (Píldora superior derecha)
    private List<String> tags;            // Múltiples badges (ej: ["Parciales complejos", "Zona silenciosa"])
    private String targetUrl;             // Link principal al hacer clic en la tarjeta
    private String statusText;            // Ej: "ABIERTO · CIERRA 8:00 PM"
    private InteractionStats stats;       // Me gusta y comentarios
    private Status status;                // Estado del contenido (UNDER_REVIEW, PUBLISHED y ARCHIVED)

    private Content(ContentId id,
                   String title,
                   String subtitle,
                   String description,
                   String imageUrl,
                   String categoryTag,
                   List<String> tags,
                   String targetUrl,
                   String statusText,
                   InteractionStats stats,
                   Status status) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.title = Objects.requireNonNull(title, "Titulo es obligatorio");
        this.subtitle = subtitle;
        this.description = description;
        this.imageUrl = imageUrl;
        this.categoryTag = categoryTag;
        this.tags = tags != null ? tags : List.of();
        this.targetUrl = Objects.requireNonNull(targetUrl, "La ruta de destino (targetUrl) es obligatoria");
        this.statusText = statusText;
        this.stats = stats != null ? stats : InteractionStats.empty();
        this.status = status != null ? status : Status.UNDER_REVIEW;
    }

    public static Content of(ContentId id, String title, String subtitle, String description,
                             String imageUrl, String categoryTag, List<String> tags,
                             String targetUrl, String statusText, InteractionStats stats, Status status) {
        return new Content(
                id, title, subtitle, description, imageUrl,
                categoryTag, tags, targetUrl, statusText, stats, status
        );
    }

    public static Content create(String title, String subtitle, String description,
                                 String imageUrl, String categoryTag, List<String> tags,
                                 String targetUrl, String statusText, InteractionStats stats, Status status) {
        return new Content(
                ContentId.generate(),
                title, subtitle, description, imageUrl,
                categoryTag, tags, targetUrl, statusText, stats, status
        );
    }

    public void publicarContenido() {
        if(status != Status.UNDER_REVIEW) {
            throw  new DomainException("SOLO SE PUEDE PUBLICAR CONTENIDO EN REVISION");
        } else {
            this.status = Status.PUBLISHED;
        }
    }

    public void archivar() {
        this.status = Status.ARCHIVED;
    }

    public ContentId id() {return id;}
    public String title() {return title;}
    public String subtitle() {return subtitle;}
    public String description() {return description;}
    public String imageUrl() {return imageUrl;}
    public String categoryTag() {return categoryTag;}
    public List<String> tags() {return tags;}
    public String targetUrl() {return targetUrl;}
    public String statusText() {return statusText;}
    public InteractionStats stats() {return stats;}

}
