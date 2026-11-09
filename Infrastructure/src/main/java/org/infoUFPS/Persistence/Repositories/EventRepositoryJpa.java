package org.infoUFPS.Persistence.Repositories;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.infoUFPS.Criterias.EventDuplicateCriteria;
import org.infoUFPS.Criterias.EventPageCriteria;
import org.infoUFPS.Criterias.EventRangeCriteria;
import org.infoUFPS.Criterias.ModerationCriteria;
import org.infoUFPS.Entities.Event;
import org.infoUFPS.Enums.Status;
import org.infoUFPS.Persistence.EntityConfigurations.EventJpa;
import org.infoUFPS.Repositories.IEventRepository;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.EventId;
import org.infoUFPS.ValueObjects.PageRequest;
import org.infoUFPS.ValueObjects.PagedResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Adaptador JPA del puerto {@link IEventRepository}.
 */
@ApplicationScoped
public class EventRepositoryJpa implements IEventRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public Event save(Event event) {
        entityManager.merge(EventJpaMapper.toEntity(event));
        return event;
    }

    @Override
    public Optional<Event> findById(EventId id) {
        EventJpa entity = entityManager.find(EventJpa.class, id.value());
        return Optional.ofNullable(entity).map(EventJpaMapper::toDomain);
    }

    @Override
    public List<Event> findPublishedOverlapping(EventRangeCriteria criteria) {
        StringBuilder jpql = new StringBuilder(
                "SELECT e FROM EventJpa e " +
                        "WHERE e.status = :status AND e.startDate <= :to AND e.endDate >= :from");
        if (criteria.hasTypes()) {
            jpql.append(" AND e.type IN :types");
        }
        jpql.append(" ORDER BY CASE WHEN e.startTime IS NULL THEN 0 ELSE 1 END, ")
                .append("e.startTime ASC, e.startDate ASC, LOWER(e.title) ASC");

        TypedQuery<EventJpa> query = entityManager.createQuery(jpql.toString(), EventJpa.class)
                .setParameter("status", Status.PUBLISHED)
                .setParameter("from", criteria.from())
                .setParameter("to", criteria.to());
        if (criteria.hasTypes()) {
            query.setParameter("types", criteria.types());
        }
        return query.getResultList().stream().map(EventJpaMapper::toDomain).toList();
    }

    @Override
    public PagedResult<Event> findPublishedUpcomingOrOngoing(EventPageCriteria criteria) {
        String base = " FROM EventJpa e WHERE e.status = :status AND e.endDate >= :today";
        String typeFilter = criteria.hasTypes() ? " AND e.type IN :types" : "";

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "SELECT COUNT(e)" + base + typeFilter, Long.class)
                .setParameter("status", Status.PUBLISHED)
                .setParameter("today", criteria.today());
        if (criteria.hasTypes()) {
            countQuery.setParameter("types", criteria.types());
        }
        Long total = countQuery.getSingleResult();

        TypedQuery<EventJpa> dataQuery = entityManager.createQuery(
                        "SELECT e" + base + typeFilter +
                                " ORDER BY e.startDate ASC, CASE WHEN e.startTime IS NULL THEN 0 ELSE 1 END, " +
                                "e.startTime ASC, LOWER(e.title) ASC",
                        EventJpa.class)
                .setParameter("status", Status.PUBLISHED)
                .setParameter("today", criteria.today())
                .setFirstResult((int) criteria.pageRequest().offset())
                .setMaxResults(criteria.pageRequest().size());
        if (criteria.hasTypes()) {
            dataQuery.setParameter("types", criteria.types());
        }

        List<Event> events = dataQuery.getResultList().stream().map(EventJpaMapper::toDomain).toList();
        return PagedResult.of(events, total != null ? total : 0L, criteria.pageRequest());
    }

    @Override
    public PagedResult<Event> findByProposedBy(AccountId accountId, PageRequest pageRequest) {
        Long total = entityManager.createQuery(
                        "SELECT COUNT(e) FROM EventJpa e WHERE e.proposedBy = :account", Long.class)
                .setParameter("account", accountId.value())
                .getSingleResult();

        List<Event> events = entityManager.createQuery(
                        "SELECT e FROM EventJpa e WHERE e.proposedBy = :account ORDER BY e.createdAt DESC",
                        EventJpa.class)
                .setParameter("account", accountId.value())
                .setFirstResult((int) pageRequest.offset())
                .setMaxResults(pageRequest.size())
                .getResultList()
                .stream()
                .map(EventJpaMapper::toDomain)
                .toList();

        return PagedResult.of(events, total != null ? total : 0L, pageRequest);
    }

    @Override
    public PagedResult<Event> findByStatus(ModerationCriteria criteria) {
        Long total = entityManager.createQuery(
                        "SELECT COUNT(e) FROM EventJpa e WHERE e.status = :status", Long.class)
                .setParameter("status", criteria.status())
                .getSingleResult();

        List<Event> events = entityManager.createQuery(
                        "SELECT e FROM EventJpa e WHERE e.status = :status ORDER BY e.createdAt ASC",
                        EventJpa.class)
                .setParameter("status", criteria.status())
                .setFirstResult((int) criteria.pageRequest().offset())
                .setMaxResults(criteria.pageRequest().size())
                .getResultList()
                .stream()
                .map(EventJpaMapper::toDomain)
                .toList();

        return PagedResult.of(events, total != null ? total : 0L, criteria.pageRequest());
    }

    @Override
    public List<Event> findDuplicates(EventDuplicateCriteria criteria) {
        StringBuilder jpql = new StringBuilder(
                "SELECT e FROM EventJpa e " +
                        "WHERE LOWER(e.title) = :title AND e.startDate = :startDate " +
                        "AND e.status <> :archived");
        if (criteria.startTime() == null) {
            jpql.append(" AND e.startTime IS NULL");
        } else {
            jpql.append(" AND e.startTime = :startTime");
        }
        if (criteria.excludeId() != null) {
            jpql.append(" AND e.eventId <> :excludeId");
        }
        jpql.append(" ORDER BY e.createdAt ASC");

        TypedQuery<EventJpa> query = entityManager.createQuery(jpql.toString(), EventJpa.class)
                .setParameter("title", criteria.title().toLowerCase(Locale.ROOT))
                .setParameter("startDate", criteria.startDate())
                .setParameter("archived", Status.ARCHIVED);
        if (criteria.startTime() != null) {
            query.setParameter("startTime", criteria.startTime());
        }
        if (criteria.excludeId() != null) {
            query.setParameter("excludeId", criteria.excludeId().value());
        }
        return query.getResultList().stream().map(EventJpaMapper::toDomain).toList();
    }

    @Override
    public long countProposalsByAccountOnDate(AccountId accountId, LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        Long count = entityManager.createQuery(
                        "SELECT COUNT(e) FROM EventJpa e " +
                                "WHERE e.proposedBy = :account AND e.createdAt >= :from AND e.createdAt < :to",
                        Long.class)
                .setParameter("account", accountId.value())
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    @Transactional
    public void deleteById(EventId id) {
        entityManager.createQuery("DELETE FROM EventJpa e WHERE e.eventId = :id")
                .setParameter("id", id.value())
                .executeUpdate();
    }

    @Override
    public boolean existsById(EventId id) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(e) FROM EventJpa e WHERE e.eventId = :id", Long.class)
                .setParameter("id", id.value())
                .getSingleResult();
        return count != null && count > 0;
    }
}
