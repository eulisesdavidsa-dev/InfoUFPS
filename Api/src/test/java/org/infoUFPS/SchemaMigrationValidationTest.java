package org.infoUFPS;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprueba que las migraciones Flyway producen un esquema compatible con el
 * mapeo de Hibernate y con las reglas exigidas al módulo académico.
 * <p>
 * Se ejecuta sobre PostgreSQL embebido porque H2 no soporta índices parciales.
 */
@QuarkusTest
@QuarkusTestResource(value = EmbeddedPostgresResource.class, restrictToAnnotatedClass = true)
@TestProfile(SchemaValidationProfile.class)
@EnabledOnOs(OS.WINDOWS)
@DisplayName("Migraciones Flyway compatibles con Hibernate")
class SchemaMigrationValidationTest {

    @Inject
    DataSource dataSource;

    @Test
    @DisplayName("Flyway aplica V1, V2 y V3 y Hibernate valida el esquema")
    void migrationsAreAppliedAndValidated() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE")) {
            assertTrue(result.next());
            assertEquals(3, result.getInt(1), "Deben aplicarse las tres migraciones");
        }
    }

    @Test
    @DisplayName("El índice único parcial de profesores publicados existe")
    void publishedProfessorUniqueIndexExists() throws SQLException {
        assertEquals(1, count(
                "SELECT COUNT(*) FROM pg_indexes " +
                        "WHERE indexname = 'idx_unique_published_profesor_nombre_facultad'"));
    }

    @Test
    @DisplayName("Las reseñas no permiten borrar profesores o materias con reseñas")
    void reviewForeignKeysUseRestrict() throws SQLException {
        assertEquals("r", scalar(
                "SELECT confdeltype FROM pg_constraint WHERE conname = 'fk_resena_profesor'"));
        assertEquals("r", scalar(
                "SELECT confdeltype FROM pg_constraint WHERE conname = 'fk_resena_materia'"));
    }

    @Test
    @DisplayName("Los contadores de interacción son obligatorios y valen cero por defecto")
    void interactionCountersAreNotNullWithZeroDefault() throws SQLException {
        assertEquals("NO", scalar(
                "SELECT is_nullable FROM information_schema.columns " +
                        "WHERE table_name = 'contents' AND column_name = 'likes_count'"));
        assertEquals("NO", scalar(
                "SELECT is_nullable FROM information_schema.columns " +
                        "WHERE table_name = 'contents' AND column_name = 'comments_count'"));
        assertEquals("0", scalar(
                "SELECT column_default FROM information_schema.columns " +
                        "WHERE table_name = 'contents' AND column_name = 'likes_count'"));
        assertEquals("0", scalar(
                "SELECT column_default FROM information_schema.columns " +
                        "WHERE table_name = 'contents' AND column_name = 'comments_count'"));
    }

    private int count(String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getInt(1);
        }
    }

    private String scalar(String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next(), "La consulta no devolvió filas: " + sql);
            return result.getString(1);
        }
    }
}
