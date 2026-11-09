package org.infoUFPS;

import org.infoUFPS.Criterias.MateriaSearchCriteria;
import org.infoUFPS.Criterias.ProfesorSearchCriteria;
import org.infoUFPS.DTOs.MateriaDetailDto;
import org.infoUFPS.DTOs.ModeracionWarningDto;
import org.infoUFPS.DTOs.ProfesorDetailDto;
import org.infoUFPS.DTOs.Requests.*;
import org.infoUFPS.DTOs.ResenaDto;
import org.infoUFPS.Entities.Account;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Entities.Profesor;
import org.infoUFPS.Entities.Resena;
import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Enums.Facultad;
import org.infoUFPS.Enums.Role;
import org.infoUFPS.Enums.TipoDestino;
import org.infoUFPS.Enums.TipoMateria;
import org.infoUFPS.Exceptions.DailyLimitExceededException;
import org.infoUFPS.Exceptions.DuplicateResourceException;
import org.infoUFPS.Features.Materia.Commands.AdminCreateMateriaHandler;
import org.infoUFPS.Features.Materia.Commands.ProposeMateriaHandler;
import org.infoUFPS.Features.Moderacion.Commands.ModerateProfesorHandler;
import org.infoUFPS.Features.Profesor.Commands.AdminCreateProfesorHandler;
import org.infoUFPS.Features.Profesor.Commands.ProposeProfesorHandler;
import org.infoUFPS.Features.Resena.Commands.CreateResenaHandler;
import org.infoUFPS.Features.Resena.Commands.DeleteResenaHandler;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.Repositories.IAccountRepository;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.Repositories.IProfesorRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.Services.DailyLimitService;
import org.infoUFPS.Services.SimilarityService;
import org.infoUFPS.Utils.TextNormalizer;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.MateriaId;
import org.infoUFPS.ValueObjects.ProfesorId;
import org.infoUFPS.ValueObjects.ResenaId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationUseCasesTest {

    private InMemoryProfesorRepository profesorRepo;
    private InMemoryMateriaRepository materiaRepo;
    private InMemoryResenaRepository resenaRepo;
    private InMemoryAccountRepository accountRepo;
    private DailyLimitService dailyLimitService;
    private SimilarityService similarityService;

    private ProposeProfesorHandler proposeProfesorHandler;
    private AdminCreateProfesorHandler adminCreateProfesorHandler;
    private ProposeMateriaHandler proposeMateriaHandler;
    private AdminCreateMateriaHandler adminCreateMateriaHandler;
    private CreateResenaHandler createResenaHandler;
    private DeleteResenaHandler deleteResenaHandler;
    private ModerateProfesorHandler moderateProfesorHandler;

    private Account testUser;

    @BeforeEach
    void setUp() {
        profesorRepo = new InMemoryProfesorRepository();
        materiaRepo = new InMemoryMateriaRepository();
        resenaRepo = new InMemoryResenaRepository();
        accountRepo = new InMemoryAccountRepository();

        testUser = Account.register("estudiante@ufps.edu.co", "hash123", "Estudiante UFPS", Role.USER);
        accountRepo.save(testUser);

        dailyLimitService = new DailyLimitService(profesorRepo, materiaRepo, resenaRepo);
        similarityService = new SimilarityService(profesorRepo, materiaRepo);

        proposeProfesorHandler = new ProposeProfesorHandler(profesorRepo, accountRepo, dailyLimitService);
        adminCreateProfesorHandler = new AdminCreateProfesorHandler(profesorRepo, materiaRepo);
        proposeMateriaHandler = new ProposeMateriaHandler(materiaRepo, accountRepo, dailyLimitService);
        adminCreateMateriaHandler = new AdminCreateMateriaHandler(materiaRepo);
        createResenaHandler = new CreateResenaHandler(resenaRepo, profesorRepo, materiaRepo, accountRepo, dailyLimitService);
        deleteResenaHandler = new DeleteResenaHandler(resenaRepo);
        moderateProfesorHandler = new ModerateProfesorHandler(profesorRepo, resenaRepo);
    }

    @Test
    @DisplayName("No se puede duplicar un profesor publicado por nombre normalizado y facultad")
    void testCannotDuplicatePublishedProfesor() {
        // Admin crea profesor en Ciencias Básicas
        adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Pérez, Juan Carlos", Facultad.CIENCIAS_BASICAS, "juan.perez@ufps.edu.co", Set.of()));

        // Usuario intenta proponer profesor con orden de palabras desordenado y tildes en la misma facultad
        ProposeProfesorCommand dupCommand = new ProposeProfesorCommand("Juan Carlos Perez", Facultad.CIENCIAS_BASICAS, null, testUser.getAccountId().value());

        assertThrows(DuplicateResourceException.class, () -> proposeProfesorHandler.execute(dupCommand));

        // Pero en OTRA facultad sí es permitido
        assertDoesNotThrow(() ->
                proposeProfesorHandler.execute(new ProposeProfesorCommand("Juan Carlos Perez", Facultad.INGENIERIA, null, testUser.getAccountId().value())));
    }

    @Test
    @DisplayName("Validación de email único para profesores")
    void testUniqueProfesorEmail() {
        adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Profesor Uno", Facultad.INGENIERIA, "repetido@ufps.edu.co", Set.of()));

        // Intentar registrar otro profesor con el mismo email (incluso con distinto case) debe fallar
        ProposeProfesorCommand dupEmail = new ProposeProfesorCommand("Profesor Dos", Facultad.CIENCIAS_DE_LA_SALUD, "REPETIDO@ufps.edu.co", testUser.getAccountId().value());
        assertThrows(DuplicateResourceException.class, () -> proposeProfesorHandler.execute(dupEmail));
    }

    @Test
    @DisplayName("Materias: código único cuando existe")
    void testUniqueMateriaCodigo() {
        adminCreateMateriaHandler.execute(new AdminSaveMateriaCommand("Cálculo I", "1150101", 4, TipoMateria.CARRERA));

        // Proponer materia con el mismo código debe fallar
        ProposeMateriaCommand dupCode = new ProposeMateriaCommand("Cálculo Diferencial", "1150101", 3, TipoMateria.CARRERA, testUser.getAccountId().value());
        assertThrows(DuplicateResourceException.class, () -> proposeMateriaHandler.execute(dupCode));
    }

    @Test
    @DisplayName("Unicidad de reseñas activas por usuario y destino")
    void testActiveReviewUniqueness() {
        // Crear profesor publicado
        ProfesorDetailDto prof = adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Profesor Titular", Facultad.INGENIERIA, null, Set.of()));

        // Crear primera reseña
        CreateResenaCommand cmd1 = new CreateResenaCommand(testUser.getAccountId().value(), TipoDestino.PROFESOR, prof.id(), null, 5, "Excelente", "BD");
        createResenaHandler.execute(cmd1);

        // Intentar crear segunda reseña mientras la primera esté activa debe fallar
        CreateResenaCommand cmd2 = new CreateResenaCommand(testUser.getAccountId().value(), TipoDestino.PROFESOR, prof.id(), null, 4, "Otra opinión", "BD");
        assertThrows(DuplicateResourceException.class, () -> createResenaHandler.execute(cmd2));

        // Si el usuario elimina (archiva) su reseña anterior, ahora sí puede crear una nueva
        Resena primera = resenaRepo.findActiveByUserAndProfesor(testUser.getAccountId(), ProfesorId.of(prof.id())).orElseThrow();
        deleteResenaHandler.execute(primera.getId().value(), testUser.getAccountId().value(), false);

        assertDoesNotThrow(() -> createResenaHandler.execute(cmd2));
    }

    @Test
    @DisplayName("Promedios y conteos solo se calculan sobre reseñas publicadas")
    void testAverageRatingOnlyPublished() {
        ProfesorDetailDto prof = adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Docente Evaluado", Facultad.INGENIERIA, null, Set.of()));
        ProfesorId profId = ProfesorId.of(prof.id());

        // Reseña 1: en revisión (calif 5) -> promedio no debe contarla
        Resena r1 = Resena.createForProfesor(testUser.getAccountId(), profId, 5, "Comentario 1", null);
        resenaRepo.save(r1);
        assertNull(resenaRepo.getAverageRatingForProfesor(profId));
        assertEquals(0, resenaRepo.countPublishedByProfesor(profId));

        // Publicar reseña 1
        r1.publish();
        resenaRepo.save(r1);
        assertEquals(5.0, resenaRepo.getAverageRatingForProfesor(profId));
        assertEquals(1, resenaRepo.countPublishedByProfesor(profId));

        // Reseña 2: publicada (calif 3)
        Resena r2 = Resena.createForProfesor(AccountId.generate(), profId, 3, "Comentario 2", null);
        r2.publish();
        resenaRepo.save(r2);
        assertEquals(4.0, resenaRepo.getAverageRatingForProfesor(profId));
        assertEquals(2, resenaRepo.countPublishedByProfesor(profId));

        // Reseña 3: archivada (calif 1) -> no debe afectar el promedio
        Resena r3 = Resena.createForProfesor(AccountId.generate(), profId, 1, "Comentario 3", null);
        r3.archive();
        resenaRepo.save(r3);
        assertEquals(4.0, resenaRepo.getAverageRatingForProfesor(profId));
        assertEquals(2, resenaRepo.countPublishedByProfesor(profId));
    }

    @Test
    @DisplayName("Límite diario de propuestas")
    void testDailyLimitEnforcement() {
        // Enviar propuestas hasta el límite diario (10)
        for (int i = 1; i <= DailyLimitService.MAX_DAILY_PROFESOR_PROPOSALS; i++) {
            proposeProfesorHandler.execute(new ProposeProfesorCommand("Profesor Número " + i, Facultad.INGENIERIA, null, testUser.getAccountId().value()));
        }

        // La 11va debe ser rechazada con DailyLimitExceededException
        ProposeProfesorCommand extra = new ProposeProfesorCommand("Profesor Extra", Facultad.INGENIERIA, null, testUser.getAccountId().value());
        assertThrows(DailyLimitExceededException.class, () -> proposeProfesorHandler.execute(extra));
    }

    @Test
    @DisplayName("Detección de similitud advierte al admin sobre nombres similares")
    void testSimilarityDetection() {
        adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Roberto Carlos Gómez", Facultad.INGENIERIA, null, Set.of()));

        List<ModeracionWarningDto> warnings = similarityService.checkProfesorSimilarity("Roberto C. Gómez", Facultad.INGENIERIA);
        assertFalse(warnings.isEmpty(), "Debe encontrar coincidencia con similitud >= 70%");
        assertTrue(warnings.getFirst().similitud() >= 0.70);
    }

    @Test
    @DisplayName("Moderación: publicar con correcciones de admin")
    void testModerateWithCorrections() {
        ProfesorDetailDto proposal = proposeProfesorHandler.execute(
                new ProposeProfesorCommand("Profe Con Errores", Facultad.NO_ESPECIFICADA, null, testUser.getAccountId().value()));

        ModerateEntityCommand correction = new ModerateEntityCommand("Profesor Corregido", Facultad.INGENIERIA, "corregido@ufps.edu.co", null, null, null);
        ProfesorDetailDto published = moderateProfesorHandler.publish(proposal.id(), correction);

        assertEquals("Profesor Corregido", published.nombre());
        assertEquals(Facultad.INGENIERIA, published.facultad());
        assertEquals("corregido@ufps.edu.co", published.email());
        assertEquals(EstadoModeracion.PUBLICADO, published.estado());
    }

    // ─────────────────────────────────────────────────────────────────────
    // Repositorios en memoria para pruebas rápidas y deterministas
    // ─────────────────────────────────────────────────────────────────────

    static class InMemoryProfesorRepository implements IProfesorRepository {
        Map<String, Profesor> db = new HashMap<>();

        @Override public Profesor save(Profesor p) { db.put(p.getId().value(), p); return p; }
        @Override public Optional<Profesor> findById(ProfesorId id) { return Optional.ofNullable(db.get(id.value())); }
        @Override
        public PageResult<Profesor> search(ProfesorSearchCriteria criteria, PageRequest pr) {
            List<Profesor> list = db.values().stream()
                    .filter(p -> criteria.estado() == null || p.getEstado() == criteria.estado())
                    .filter(p -> criteria.facultad() == null || p.getFacultad() == criteria.facultad())
                    .filter(p -> criteria.query() == null || TextNormalizer.matchesQueryTokens(p.getNombre(), criteria.query()))
                    .sorted(Comparator.comparing(Profesor::getNombre))
                    .toList();
            return PageResult.of(list, pr.page(), pr.size(), list.size());
        }
        @Override
        public boolean existsByNombreNormalizadoAndFacultadAndEstado(String norm, Facultad fac, EstadoModeracion est) {
            return db.values().stream().anyMatch(p -> p.getNombreNormalizado().equals(norm) && p.getFacultad() == fac && p.getEstado() == est);
        }
        @Override
        public boolean existsByEmail(String email) {
            return db.values().stream().anyMatch(p -> p.getEmail() != null && p.getEmail().equalsIgnoreCase(email.trim()));
        }
        @Override
        public boolean existsByEmailAndNotId(String email, ProfesorId excludeId) {
            return db.values().stream().anyMatch(p -> !p.getId().equals(excludeId) && p.getEmail() != null && p.getEmail().equalsIgnoreCase(email.trim()));
        }
        @Override
        public List<Profesor> findSimilar(String norm, Facultad fac) {
            return db.values().stream()
                    .filter(p -> p.getEstado() != EstadoModeracion.ARCHIVADO)
                    .filter(p -> fac == null || fac == Facultad.NO_ESPECIFICADA || p.getFacultad() == fac)
                    .toList();
        }
        @Override
        public List<Profesor> findByProponenteId(AccountId pid) {
            return db.values().stream().filter(p -> pid.equals(p.getProponenteId())).toList();
        }
        @Override
        public long countTodaySubmissionsByProponente(AccountId pid) {
            return db.values().stream()
                    .filter(p -> pid.equals(p.getProponenteId()) && p.getCreatedAt().toLocalDate().isEqual(LocalDate.now()))
                    .count();
        }
        @Override public void deleteById(ProfesorId id) { db.remove(id.value()); }
    }

    static class InMemoryMateriaRepository implements IMateriaRepository {
        Map<String, Materia> db = new HashMap<>();

        @Override public Materia save(Materia m) { db.put(m.getId().value(), m); return m; }
        @Override public Optional<Materia> findById(MateriaId id) { return Optional.ofNullable(db.get(id.value())); }
        @Override
        public Optional<Materia> findByCodigo(String code) {
            return db.values().stream().filter(m -> m.getCodigo() != null && m.getCodigo().equalsIgnoreCase(code.trim())).findFirst();
        }
        @Override
        public PageResult<Materia> search(MateriaSearchCriteria criteria, PageRequest pr) {
            List<Materia> list = db.values().stream()
                    .filter(m -> criteria.estado() == null || m.getEstado() == criteria.estado())
                    .filter(m -> criteria.creditos() == null || Objects.equals(m.getCreditos(), criteria.creditos()))
                    .filter(m -> criteria.tipo() == null || m.getTipo() == criteria.tipo())
                    .filter(m -> criteria.query() == null || (m.getCodigo() != null && m.getCodigo().equalsIgnoreCase(criteria.query().trim()))
                            || TextNormalizer.matchesQueryTokens(m.getNombre(), criteria.query()))
                    .sorted(Comparator.comparing(Materia::getNombre))
                    .toList();
            return PageResult.of(list, pr.page(), pr.size(), list.size());
        }
        @Override
        public boolean existsByCodigo(String code) {
            return db.values().stream().anyMatch(m -> m.getCodigo() != null && m.getCodigo().equalsIgnoreCase(code.trim()));
        }
        @Override
        public boolean existsByCodigoAndNotId(String code, MateriaId excludeId) {
            return db.values().stream().anyMatch(m -> !m.getId().equals(excludeId) && m.getCodigo() != null && m.getCodigo().equalsIgnoreCase(code.trim()));
        }
        @Override
        public List<Materia> findSimilar(String norm) {
            return db.values().stream().filter(m -> m.getEstado() != EstadoModeracion.ARCHIVADO).toList();
        }
        @Override
        public List<Materia> findByProponenteId(AccountId pid) {
            return db.values().stream().filter(m -> pid.equals(m.getProponenteId())).toList();
        }
        @Override
        public long countTodaySubmissionsByProponente(AccountId pid) {
            return db.values().stream()
                    .filter(m -> pid.equals(m.getProponenteId()) && m.getCreatedAt().toLocalDate().isEqual(LocalDate.now()))
                    .count();
        }
        @Override
        public List<Materia> findByIds(Collection<MateriaId> ids) {
            return db.values().stream().filter(m -> ids.contains(m.getId())).toList();
        }
        @Override public void deleteById(MateriaId id) { db.remove(id.value()); }
    }

    static class InMemoryResenaRepository implements IResenaRepository {
        Map<String, Resena> db = new HashMap<>();

        @Override public Resena save(Resena r) { db.put(r.getId().value(), r); return r; }
        @Override public Optional<Resena> findById(ResenaId id) { return Optional.ofNullable(db.get(id.value())); }
        @Override
        public Optional<Resena> findActiveByUserAndProfesor(AccountId aid, ProfesorId pid) {
            return db.values().stream()
                    .filter(r -> r.getAutorId().equals(aid) && pid.equals(r.getProfesorId()) && r.getEstado() != EstadoModeracion.ARCHIVADO)
                    .findFirst();
        }
        @Override
        public Optional<Resena> findActiveByUserAndMateria(AccountId aid, MateriaId mid) {
            return db.values().stream()
                    .filter(r -> r.getAutorId().equals(aid) && mid.equals(r.getMateriaId()) && r.getEstado() != EstadoModeracion.ARCHIVADO)
                    .findFirst();
        }
        @Override
        public PageResult<Resena> findPublishedByProfesor(ProfesorId pid, PageRequest pr) {
            List<Resena> list = db.values().stream()
                    .filter(r -> pid.equals(r.getProfesorId()) && r.getEstado() == EstadoModeracion.PUBLICADO)
                    .sorted(Comparator.comparing(Resena::getCreatedAt).reversed())
                    .toList();
            return PageResult.of(list, pr.page(), pr.size(), list.size());
        }
        @Override
        public PageResult<Resena> findPublishedByMateria(MateriaId mid, PageRequest pr) {
            List<Resena> list = db.values().stream()
                    .filter(r -> mid.equals(r.getMateriaId()) && r.getEstado() == EstadoModeracion.PUBLICADO)
                    .sorted(Comparator.comparing(Resena::getCreatedAt).reversed())
                    .toList();
            return PageResult.of(list, pr.page(), pr.size(), list.size());
        }
        @Override
        public List<Resena> findByAutorId(AccountId aid) {
            return db.values().stream().filter(r -> r.getAutorId().equals(aid)).toList();
        }
        @Override
        public PageResult<Resena> searchModeration(TipoDestino d, EstadoModeracion e, PageRequest pr) {
            List<Resena> list = db.values().stream()
                    .filter(r -> d == null || r.getTipoDestino() == d)
                    .filter(r -> e == null || r.getEstado() == e)
                    .toList();
            return PageResult.of(list, pr.page(), pr.size(), list.size());
        }
        @Override
        public Double getAverageRatingForProfesor(ProfesorId pid) {
            List<Integer> ratings = db.values().stream()
                    .filter(r -> pid.equals(r.getProfesorId()) && r.getEstado() == EstadoModeracion.PUBLICADO)
                    .map(Resena::getCalificacion)
                    .toList();
            return ratings.isEmpty() ? null : ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        }
        @Override
        public long countPublishedByProfesor(ProfesorId pid) {
            return db.values().stream()
                    .filter(r -> pid.equals(r.getProfesorId()) && r.getEstado() == EstadoModeracion.PUBLICADO)
                    .count();
        }
        @Override
        public Double getAverageRatingForMateria(MateriaId mid) {
            List<Integer> ratings = db.values().stream()
                    .filter(r -> mid.equals(r.getMateriaId()) && r.getEstado() == EstadoModeracion.PUBLICADO)
                    .map(Resena::getCalificacion)
                    .toList();
            return ratings.isEmpty() ? null : ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        }
        @Override
        public long countPublishedByMateria(MateriaId mid) {
            return db.values().stream()
                    .filter(r -> mid.equals(r.getMateriaId()) && r.getEstado() == EstadoModeracion.PUBLICADO)
                    .count();
        }
        @Override
        public long countTodaySubmissionsByAutor(AccountId aid) {
            return db.values().stream()
                    .filter(r -> aid.equals(r.getAutorId()) && r.getCreatedAt().toLocalDate().isEqual(LocalDate.now()))
                    .count();
        }
        @Override public void deleteById(ResenaId id) { db.remove(id.value()); }
    }

    static class InMemoryAccountRepository implements IAccountRepository {
        Map<String, Account> db = new HashMap<>();

        @Override public Account save(Account a) { db.put(a.getAccountId().value(), a); return a; }
        @Override public Optional<Account> findById(AccountId id) { return Optional.ofNullable(db.get(id.value())); }
        @Override
        public Optional<Account> findByEmail(String email) {
            return db.values().stream().filter(a -> a.getEmail().equalsIgnoreCase(email)).findFirst();
        }
        @Override
        public boolean existsByEmail(String email) {
            return db.values().stream().anyMatch(a -> a.getEmail().equalsIgnoreCase(email));
        }
        @Override public void deleteById(AccountId id) { db.remove(id.value()); }
    }
}

