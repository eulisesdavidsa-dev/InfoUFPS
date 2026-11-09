package org.infoUFPS;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.infoUFPS.Entities.Account;
import org.infoUFPS.Enums.Role;
import org.infoUFPS.Repositories.IAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ApiIntegrationTest {

    @Inject
    IAccountRepository accountRepository;

    private String adminId;
    private String userId;

    @BeforeEach
    void setUp() {
        if (!accountRepository.existsByEmail("admin@ufps.edu.co")) {
            Account admin = Account.register("admin@ufps.edu.co", "hashAdmin", "Administrador", Role.ADMIN);
            accountRepository.save(admin);
            adminId = admin.getAccountId().value();
        } else {
            adminId = accountRepository.findByEmail("admin@ufps.edu.co").orElseThrow().getAccountId().value();
        }

        if (!accountRepository.existsByEmail("alumno@ufps.edu.co")) {
            Account user = Account.register("alumno@ufps.edu.co", "hashUser", "Alumno Test", Role.USER);
            accountRepository.save(user);
            userId = user.getAccountId().value();
        } else {
            userId = accountRepository.findByEmail("alumno@ufps.edu.co").orElseThrow().getAccountId().value();
        }
    }

    @Test
    @DisplayName("Catálogos públicos devuelven 200 OK")
    void testPublicCatalogs() {
        given()
                .when().get("/api/v1/public/catalogos/facultades")
                .then().statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].clave", notNullValue());

        given()
                .when().get("/api/v1/public/catalogos/tipos-materia")
                .then().statusCode(200)
                .body("size()", greaterThan(0));

        given()
                .when().get("/api/v1/public/catalogos/creditos")
                .then().statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("Rutas protegidas devuelven 401 si no hay usuario autenticado")
    void testUnauthorizedWithoutHeader() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"nombre\":\"Docente\",\"facultad\":\"INGENIERIA\"}")
                .when().post("/api/v1/user/profesores")
                .then().statusCode(401);

        given()
                .contentType(ContentType.JSON)
                .body("{\"nombre\":\"Docente\",\"facultad\":\"INGENIERIA\"}")
                .when().post("/api/v1/admin/profesores")
                .then().statusCode(401);
    }

    @Test
    @DisplayName("Rutas administrativas devuelven 403 si el rol no es ADMIN")
    void testForbiddenForNormalUser() {
        given()
                .header("X-User-Id", userId)
                .header("X-User-Role", "USER")
                .contentType(ContentType.JSON)
                .body("{\"nombre\":\"Docente\",\"facultad\":\"INGENIERIA\"}")
                .when().post("/api/v1/admin/profesores")
                .then().statusCode(403);
    }

    @Test
    @DisplayName("Flujo completo: Admin crea materia y profesor, usuario propone y reseña, búsqueda pública")
    void testEndToEndWorkflow() {
        // 1. Admin crea materia publicada
        String materiaId = given()
                .header("X-User-Id", adminId)
                .header("X-User-Role", "ADMIN")
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "nombre": "Arquitectura de Software",
                            "codigo": "1150801",
                            "creditos": 4,
                            "tipo": "CARRERA"
                        }
                        """)
                .when().post("/api/v1/admin/materias")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("codigo", equalTo("1150801"))
                .extract().path("id");

        // 2. Admin crea profesor publicado
        String profesorId = given()
                .header("X-User-Id", adminId)
                .header("X-User-Role", "ADMIN")
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "nombre": "Fernando José Martínez",
                            "facultad": "INGENIERIA",
                            "email": "fernando.martinez@ufps.edu.co",
                            "materiasIds": ["%s"]
                        }
                        """.formatted(materiaId))
                .when().post("/api/v1/admin/profesores")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("nombre", equalTo("Fernando José Martínez"))
                .body("email", equalTo("fernando.martinez@ufps.edu.co"))
                .extract().path("id");

        // 3. Intento de crear duplicado por admin devuelve 409 Conflict
        given()
                .header("X-User-Id", adminId)
                .header("X-User-Role", "ADMIN")
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "nombre": "Martínez Fernando José",
                            "facultad": "INGENIERIA",
                            "email": "otro@ufps.edu.co"
                        }
                        """)
                .when().post("/api/v1/admin/profesores")
                .then().statusCode(409)
                .body("error", equalTo("CONFLICT"));

        // 4. Usuario autenticado crea reseña para el profesor publicado
        String resenaId = given()
                .header("X-User-Id", userId)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "tipoDestino": "PROFESOR",
                            "profesorId": "%s",
                            "calificacion": 5,
                            "comentario": "Excelente profesor, muy claro en sus clases",
                            "materiaCursada": "Arquitectura de Software"
                        }
                        """.formatted(profesorId))
                .when().post("/api/v1/user/resenas")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("calificacion", equalTo(5))
                .body("estado", equalTo("EN_REVISION"))
                .extract().path("id");

        // 5. Usuario intenta crear segunda reseña activa para el mismo profesor -> 409 Conflict
        given()
                .header("X-User-Id", userId)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "tipoDestino": "PROFESOR",
                            "profesorId": "%s",
                            "calificacion": 4,
                            "comentario": "Otra reseña duplicada"
                        }
                        """.formatted(profesorId))
                .when().post("/api/v1/user/resenas")
                .then().statusCode(409)
                .body("error", equalTo("CONFLICT"));

        // 6. Consultar reseñas propias (el autor ve su reseña en revisión)
        given()
                .header("X-User-Id", userId)
                .when().get("/api/v1/user/resenas")
                .then().statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].id", equalTo(resenaId));

        // 7. Admin publica la reseña a través de la cola de moderación
        given()
                .header("X-User-Id", adminId)
                .header("X-User-Role", "ADMIN")
                .when().put("/api/v1/admin/moderacion/resenas/%s/publicar".formatted(resenaId))
                .then().statusCode(200)
                .body("estado", equalTo("PUBLICADO"));

        // 8. Búsqueda pública de profesor: ahora promedioCalificacion = 5.0 y totalResenas = 1
        given()
                .queryParam("q", "martinez fernando")
                .when().get("/api/v1/public/profesores")
                .then().statusCode(200)
                .body("totalElements", equalTo(1))
                .body("items[0].nombre", equalTo("Fernando José Martínez"))
                .body("items[0].promedioCalificacion", equalTo(5.0f))
                .body("items[0].totalResenas", equalTo(1));

        // 9. Detalle público del profesor
        given()
                .when().get("/api/v1/public/profesores/%s".formatted(profesorId))
                .then().statusCode(200)
                .body("id", equalTo(profesorId))
                .body("promedioCalificacion", equalTo(5.0f))
                .body("materias.size()", greaterThan(0));

        // 10. Reseñas públicas del profesor
        given()
                .when().get("/api/v1/public/profesores/%s/resenas".formatted(profesorId))
                .then().statusCode(200)
                .body("totalElements", equalTo(1))
                .body("items[0].id", equalTo(resenaId));

        // 11. Búsqueda global
        given()
                .queryParam("q", "arquitectura")
                .when().get("/api/v1/public/busqueda-global")
                .then().statusCode(200)
                .body("materias.size()", greaterThan(0));

        // 12. Consultar recurso inexistente -> 404
        given()
                .when().get("/api/v1/public/profesores/inexistente-uuid")
                .then().statusCode(404)
                .body("error", equalTo("NOT_FOUND"));
    }
}

