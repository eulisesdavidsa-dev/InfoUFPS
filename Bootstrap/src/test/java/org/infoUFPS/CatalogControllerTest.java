package org.infoUFPS;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.infoUFPS.Entities.Content;
import org.infoUFPS.Entities.Status;
import org.infoUFPS.Repositories.IContentRepository;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

@QuarkusTest
class CatalogControllerTest {

    @Inject
    IContentRepository repository;

    @Test
    void catalogRespondsOkWithoutAuthorizationHeader() {
        // No se envía ningún encabezado Authorization
        given()
                .when()
                .get("/api/v1/public/catalog")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    void catalogReturnsOnlyPublishedContentGroupedByCategory() {
        repository.save(Content.create(
                "Cálculo Diferencial", "Grupo A", "Descripción de prueba", null,
                "MATERIAS", Set.of("Ingeniería"), "/materias/calculo",
                null, Status.PUBLISHED));

        repository.save(Content.create(
                "Borrador oculto", "En revisión", "No debe verse", null,
                "MATERIAS", Set.of(), "/materias/oculto",
                null, Status.UNDER_REVIEW));

        given()
                .when()
                .get("/api/v1/public/catalog")
                .then()
                .statusCode(200)
                .body("MATERIAS.title", hasItem("Cálculo Diferencial"))
                .body("MATERIAS.title", not(hasItem("Borrador oculto")));
    }
}
