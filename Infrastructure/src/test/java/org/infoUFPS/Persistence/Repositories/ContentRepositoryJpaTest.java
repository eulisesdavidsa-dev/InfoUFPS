package org.infoUFPS.Persistence.Repositories;

import org.infoUFPS.Entities.Content;
import org.infoUFPS.Enums.ModuleType;
import org.infoUFPS.Enums.Status;
import org.infoUFPS.Persistence.EntityConfigurations.ContentJpa;
import org.infoUFPS.Persistence.EntityConfigurations.InteractionStatsJpa;
import org.infoUFPS.ValueObjects.ContentId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("ContentRepositoryJpa: contadores nulos")
class ContentRepositoryJpaTest {

    private final ContentRepositoryJpa repository = new ContentRepositoryJpa();

    private ContentJpa contentJpa() {
        ContentJpa jpa = new ContentJpa();
        jpa.setContentId(ContentId.generate().value());
        jpa.setTitle("Contenido");
        jpa.setTargetUrl("https://info.ufps.edu.co");
        jpa.setStatus(Status.PUBLISHED);
        jpa.setModuleType(ModuleType.LA_VITRINA);
        return jpa;
    }

    @Test
    @DisplayName("Interpreta como cero los contadores nulos")
    void mapsNullCountersAsZero() {
        ContentJpa jpa = contentJpa();
        jpa.setStats(new InteractionStatsJpa(null, null));

        Content content = repository.toDomain(jpa);

        assertNotNull(content.getStats());
        assertEquals(0, content.getStats().getLikesCount());
        assertEquals(0, content.getStats().getCommentsCount());
    }

    @Test
    @DisplayName("Usa estadísticas vacías cuando no hay objeto embebido")
    void mapsNullStatsAsEmpty() {
        ContentJpa jpa = contentJpa();
        jpa.setStats(null);

        Content content = repository.toDomain(jpa);

        assertNotNull(content.getStats());
        assertEquals(0, content.getStats().getLikesCount());
        assertEquals(0, content.getStats().getCommentsCount());
    }

    @Test
    @DisplayName("Conserva los contadores cuando tienen valor")
    void preservesCounterValues() {
        ContentJpa jpa = contentJpa();
        jpa.setStats(new InteractionStatsJpa(3, 7));

        Content content = repository.toDomain(jpa);

        assertEquals(3, content.getStats().getLikesCount());
        assertEquals(7, content.getStats().getCommentsCount());
    }
}
