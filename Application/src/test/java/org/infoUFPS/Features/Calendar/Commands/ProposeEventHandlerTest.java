package org.infoUFPS.Features.Calendar.Commands;

import org.infoUFPS.DTOs.EventDto;
import org.infoUFPS.Entities.Event;
import org.infoUFPS.Enums.EventType;
import org.infoUFPS.Enums.Status;
import org.infoUFPS.Exceptions.ConflictException;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Features.Calendar.Support.EventDuplicateGuard;
import org.infoUFPS.Features.Calendar.Support.EventDtoAssembler;
import org.infoUFPS.Testing.FixedCalendarSettings;
import org.infoUFPS.Testing.InMemoryEventRepository;
import org.infoUFPS.Utils.BogotaTime;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.EventSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ProposeEventHandler: propuestas de usuarios")
class ProposeEventHandlerTest {

    private static final String ACCOUNT = "account-1";

    private InMemoryEventRepository repository;
    private ProposeEventHandler handler;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEventRepository();
        handler = new ProposeEventHandler(repository, new EventDuplicateGuard(repository),
                new EventDtoAssembler(), FixedCalendarSettings.defaults());
    }

    private static EventRequest request(String title, LocalDate start, LocalTime startTime, String type) {
        LocalTime endTime = startTime != null ? startTime.plusHours(2) : null;
        return new EventRequest(title, type, "Bloque A", start, start,
                startTime, endTime, "Descripción", null);
    }

    private static EventRequest futureRequest(String title) {
        LocalDate day = BogotaTime.today().plusDays(10);
        return request(title, day, LocalTime.of(8, 0), "ACADEMICO");
    }

    private Event savePublished(String title, LocalDate day, LocalTime time) {
        Event event = Event.create(title, EventType.ACADEMICO, "Bloque A",
                EventSchedule.of(day, day, time, time != null ? time.plusHours(2) : null),
                null, null, AccountId.of("admin"), true);
        event.publish();
        return repository.save(event);
    }

    @Test
    @DisplayName("Crea una propuesta en estado UNDER_REVIEW")
    void createsProposalUnderReview() {
        EventDto dto = handler.execute(new ProposeEventCommand(ACCOUNT, futureRequest("Feria de ciencia")));

        assertEquals(Status.UNDER_REVIEW, dto.status());
        assertEquals(ACCOUNT, dto.proposedBy());
        assertEquals("Usuario", dto.authorName());
        assertEquals(1, repository.size());
    }

    @Test
    @DisplayName("Rechaza una propuesta con fecha pasada")
    void rejectsPastDate() {
        LocalDate yesterday = BogotaTime.today().minusDays(1);

        assertThrows(DomainException.class, () -> handler.execute(new ProposeEventCommand(
                ACCOUNT, request("Pasado", yesterday, LocalTime.of(8, 0), "ACADEMICO"))));
    }

    @Test
    @DisplayName("Rechaza los tipos reservados al administrador")
    void rejectsAdminOnlyTypes() {
        assertThrows(DomainException.class, () -> handler.execute(new ProposeEventCommand(
                ACCOUNT, request("Institucional", BogotaTime.today().plusDays(2), null, "INSTITUCIONAL"))));
        assertThrows(DomainException.class, () -> handler.execute(new ProposeEventCommand(
                ACCOUNT, request("Calendario", BogotaTime.today().plusDays(2), null, "CALENDARIO_ACADEMICO"))));
    }

    @Test
    @DisplayName("Rechaza un duplicado de un evento publicado")
    void rejectsPublishedDuplicate() {
        LocalDate day = BogotaTime.today().plusDays(10);
        savePublished("Feria de ciencia", day, LocalTime.of(8, 0));

        assertThrows(ConflictException.class, () -> handler.execute(new ProposeEventCommand(
                ACCOUNT, request("feria de ciencia", day, LocalTime.of(8, 0), "ACADEMICO"))));
    }

    @Test
    @DisplayName("Aplica el límite diario configurable de propuestas")
    void enforcesDailyProposalLimit() {
        ProposeEventHandler limited = new ProposeEventHandler(repository,
                new EventDuplicateGuard(repository), new EventDtoAssembler(),
                new FixedCalendarSettings(1, "https://info.ufps.edu.co", "/eventos/{id}",
                        "https://api.ufps.edu.co", "https://info.ufps.edu.co/logo.png"));

        limited.execute(new ProposeEventCommand(ACCOUNT, futureRequest("Primera")));

        assertThrows(ConflictException.class, () -> limited.execute(
                new ProposeEventCommand(ACCOUNT, futureRequest("Segunda"))));
    }

    @Test
    @DisplayName("La cuenta es obligatoria")
    void accountIsRequired() {
        assertThrows(DomainException.class, () -> handler.execute(
                new ProposeEventCommand(" ", futureRequest("Feria"))));
    }

    @Test
    @DisplayName("El autor administrador se muestra como Admin")
    void adminAuthorName() {
        LocalDate day = BogotaTime.today().plusDays(10);
        Event event = savePublished("Evento admin", day, null);

        EventDto dto = new EventDtoAssembler().toDto(event);

        assertEquals("Admin", dto.authorName());
        assertNotNull(dto.publishedAt());
    }
}
