package org.infoUFPS;

import org.infoUFPS.Criterias.MateriaSearchCriteria;
import org.infoUFPS.Criterias.ProfesorSearchCriteria;
import org.infoUFPS.DTOs.MateriaListItemDto;
import org.infoUFPS.DTOs.ProfesorListItemDto;
import org.infoUFPS.DTOs.Requests.AdminSaveMateriaCommand;
import org.infoUFPS.DTOs.Requests.AdminSaveProfesorCommand;
import org.infoUFPS.DTOs.Requests.ProposeProfesorCommand;
import org.infoUFPS.Entities.Account;
import org.infoUFPS.Enums.Facultad;
import org.infoUFPS.Enums.Role;
import org.infoUFPS.Enums.TipoMateria;
import org.infoUFPS.Features.Materia.Commands.AdminCreateMateriaHandler;
import org.infoUFPS.Features.Materia.Queries.GetMateriasPublicQuery;
import org.infoUFPS.Features.Profesor.Commands.AdminCreateProfesorHandler;
import org.infoUFPS.Features.Profesor.Commands.ProposeProfesorHandler;
import org.infoUFPS.Features.Profesor.Queries.GetProfesoresPublicQuery;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.Services.DailyLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SearchAndFiltersTest {

    private ApplicationUseCasesTest.InMemoryProfesorRepository profesorRepo;
    private ApplicationUseCasesTest.InMemoryMateriaRepository materiaRepo;
    private ApplicationUseCasesTest.InMemoryResenaRepository resenaRepo;
    private ApplicationUseCasesTest.InMemoryAccountRepository accountRepo;

    private AdminCreateProfesorHandler adminCreateProfesorHandler;
    private ProposeProfesorHandler proposeProfesorHandler;
    private AdminCreateMateriaHandler adminCreateMateriaHandler;
    private GetProfesoresPublicQuery getProfesoresPublicQuery;
    private GetMateriasPublicQuery getMateriasPublicQuery;

    private Account testUser;

    @BeforeEach
    void setUp() {
        profesorRepo = new ApplicationUseCasesTest.InMemoryProfesorRepository();
        materiaRepo = new ApplicationUseCasesTest.InMemoryMateriaRepository();
        resenaRepo = new ApplicationUseCasesTest.InMemoryResenaRepository();
        accountRepo = new ApplicationUseCasesTest.InMemoryAccountRepository();

        testUser = Account.register("user@ufps.edu.co", "hash", "Usuario Test", Role.USER);
        accountRepo.save(testUser);

        DailyLimitService dailyLimitService = new DailyLimitService(profesorRepo, materiaRepo, resenaRepo);

        adminCreateProfesorHandler = new AdminCreateProfesorHandler(profesorRepo, materiaRepo);
        proposeProfesorHandler = new ProposeProfesorHandler(profesorRepo, accountRepo, dailyLimitService);
        adminCreateMateriaHandler = new AdminCreateMateriaHandler(materiaRepo);

        getProfesoresPublicQuery = new GetProfesoresPublicQuery(profesorRepo, resenaRepo);
        getMateriasPublicQuery = new GetMateriasPublicQuery(materiaRepo, resenaRepo);
    }

    @Test
    @DisplayName("Búsqueda de profesor con tildes, mayúsculas y palabras desordenadas")
    void testProfesorSearchAccentsCaseAndDisorder() {
        adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Álvaro José Gómez", Facultad.INGENIERIA, "alvaro@ufps.edu.co", Set.of()));
        adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("María Camila Gómez", Facultad.CIENCIAS_BASICAS, "maria@ufps.edu.co", Set.of()));

        // Buscar con palabras desordenadas y sin tildes: "gomez alvaro"
        PageResult<ProfesorListItemDto> result1 = getProfesoresPublicQuery.execute("gomez alvaro", null, PageRequest.of(1, 10));
        assertEquals(1, result1.totalElements());
        assertEquals("Álvaro José Gómez", result1.items().getFirst().nombre());

        // Buscar con mayúsculas y tildes desordenadas: "JOSÉ ÁLVARO"
        PageResult<ProfesorListItemDto> result2 = getProfesoresPublicQuery.execute("JOSÉ ÁLVARO", null, PageRequest.of(1, 10));
        assertEquals(1, result2.totalElements());
        assertEquals("Álvaro José Gómez", result2.items().getFirst().nombre());

        // Buscar "GOMEZ" debe encontrar ambos profesores
        PageResult<ProfesorListItemDto> result3 = getProfesoresPublicQuery.execute("GOMEZ", null, PageRequest.of(1, 10));
        assertEquals(2, result3.totalElements());
    }

    @Test
    @DisplayName("Búsqueda con caracteres especiales (% y _) y filtro por facultad")
    void testProfesorSearchSpecialCharsAndFacultyFilter() {
        adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Profesor Especial 100%_Aprobado", Facultad.INGENIERIA, null, Set.of()));
        adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Profesor Normal", Facultad.CIENCIAS_EMPRESARIALES, null, Set.of()));

        // Búsqueda con '%' y '_'
        PageResult<ProfesorListItemDto> resultSpecial = getProfesoresPublicQuery.execute("100%_", null, PageRequest.of(1, 10));
        assertEquals(1, resultSpecial.totalElements());
        assertEquals("Profesor Especial 100%_Aprobado", resultSpecial.items().getFirst().nombre());

        // Filtro por facultad Ingeniería
        PageResult<ProfesorListItemDto> resultIng = getProfesoresPublicQuery.execute(null, Facultad.INGENIERIA, PageRequest.of(1, 10));
        assertEquals(1, resultIng.totalElements());

        // Filtro por facultad Ciencias de la Salud (no hay ninguno)
        PageResult<ProfesorListItemDto> resultSalud = getProfesoresPublicQuery.execute(null, Facultad.CIENCIAS_DE_LA_SALUD, PageRequest.of(1, 10));
        assertEquals(0, resultSalud.totalElements());
    }

    @Test
    @DisplayName("Exclusión de registros no publicados del listado público")
    void testExclusionOfNonPublishedRecords() {
        // 1 publicado
        adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Profesor Aprobado", Facultad.INGENIERIA, null, Set.of()));
        // 1 en revisión
        proposeProfesorHandler.execute(new ProposeProfesorCommand("Profesor En Revisión", Facultad.INGENIERIA, null, testUser.getAccountId().value()));

        // El listado público solo debe contener el publicado
        PageResult<ProfesorListItemDto> publicResult = getProfesoresPublicQuery.execute(null, null, PageRequest.of(1, 10));
        assertEquals(1, publicResult.totalElements());
        assertEquals("Profesor Aprobado", publicResult.items().getFirst().nombre());
    }

    @Test
    @DisplayName("Materias con búsqueda combinable por nombre/código, créditos y tipo")
    void testMateriaSearchAndCombinedFilters() {
        adminCreateMateriaHandler.execute(new AdminSaveMateriaCommand("Estructuras de Datos", "1150301", 4, TipoMateria.CARRERA));
        adminCreateMateriaHandler.execute(new AdminSaveMateriaCommand("Bases de Datos", "1150302", 4, TipoMateria.CARRERA));
        adminCreateMateriaHandler.execute(new AdminSaveMateriaCommand("Inteligencia Artificial", "1150501", 3, TipoMateria.ELECTIVA));
        adminCreateMateriaHandler.execute(new AdminSaveMateriaCommand("Ética Profesional", "1150601", 2, TipoMateria.CURSO_DE_FORMACION));

        // 1. Búsqueda por código exacto
        PageResult<MateriaListItemDto> byCode = getMateriasPublicQuery.execute("1150301", null, null, PageRequest.of(1, 10));
        assertEquals(1, byCode.totalElements());
        assertEquals("Estructuras de Datos", byCode.items().getFirst().nombre());

        // 2. Búsqueda por nombre desordenado: "datos estructuras"
        PageResult<MateriaListItemDto> byDisorderName = getMateriasPublicQuery.execute("datos estructuras", null, null, PageRequest.of(1, 10));
        assertEquals(1, byDisorderName.totalElements());
        assertEquals("Estructuras de Datos", byDisorderName.items().getFirst().nombre());

        // 3. Filtros combinados: creditos=4 y tipo=CARRERA
        PageResult<MateriaListItemDto> combined = getMateriasPublicQuery.execute(null, 4, TipoMateria.CARRERA, PageRequest.of(1, 10));
        assertEquals(2, combined.totalElements());

        // 4. Filtros combinados: creditos=3 y tipo=ELECTIVA
        PageResult<MateriaListItemDto> combinedElectiva = getMateriasPublicQuery.execute(null, 3, TipoMateria.ELECTIVA, PageRequest.of(1, 10));
        assertEquals(1, combinedElectiva.totalElements());
        assertEquals("Inteligencia Artificial", combinedElectiva.items().getFirst().nombre());

        // 5. Filtros combinados sin coincidencia: creditos=4 y tipo=ELECTIVA
        PageResult<MateriaListItemDto> noMatch = getMateriasPublicQuery.execute(null, 4, TipoMateria.ELECTIVA, PageRequest.of(1, 10));
        assertEquals(0, noMatch.totalElements());
    }

    @Test
    @DisplayName("Paginación respeta límite máximo de 50 elementos por página")
    void testPaginationMaxLimit() {
        PageRequest reqDefault = PageRequest.of(null, null);
        assertEquals(1, reqDefault.page());
        assertEquals(10, reqDefault.size());

        PageRequest reqExceeded = PageRequest.of(1, 100);
        assertEquals(50, reqExceeded.size(), "El tamaño debe acotarse al máximo de 50");
    }
}

