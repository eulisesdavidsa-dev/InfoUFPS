package org.infoUFPS.Services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.Exceptions.DailyLimitExceededException;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.Repositories.IProfesorRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.ValueObjects.AccountId;

@ApplicationScoped
public class DailyLimitService {

    public static final int MAX_DAILY_PROFESOR_PROPOSALS = 10;
    public static final int MAX_DAILY_MATERIA_PROPOSALS = 10;
    public static final int MAX_DAILY_RESENAS = 15;

    private final IProfesorRepository profesorRepository;
    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    public DailyLimitService() {
        this.profesorRepository = null;
        this.materiaRepository = null;
        this.resenaRepository = null;
    }

    @Inject
    public DailyLimitService(IProfesorRepository profesorRepository,
                             IMateriaRepository materiaRepository,
                             IResenaRepository resenaRepository) {
        this.profesorRepository = profesorRepository;
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    public void checkProfesorProposalLimit(AccountId accountId) {
        if (profesorRepository != null) {
            long count = profesorRepository.countTodaySubmissionsByProponente(accountId);
            if (count >= MAX_DAILY_PROFESOR_PROPOSALS) {
                throw new DailyLimitExceededException("Has alcanzado el límite diario de " + MAX_DAILY_PROFESOR_PROPOSALS + " propuestas de profesores");
            }
        }
    }

    public void checkMateriaProposalLimit(AccountId accountId) {
        if (materiaRepository != null) {
            long count = materiaRepository.countTodaySubmissionsByProponente(accountId);
            if (count >= MAX_DAILY_MATERIA_PROPOSALS) {
                throw new DailyLimitExceededException("Has alcanzado el límite diario de " + MAX_DAILY_MATERIA_PROPOSALS + " propuestas de materias");
            }
        }
    }

    public void checkResenaLimit(AccountId accountId) {
        if (resenaRepository != null) {
            long count = resenaRepository.countTodaySubmissionsByAutor(accountId);
            if (count >= MAX_DAILY_RESENAS) {
                throw new DailyLimitExceededException("Has alcanzado el límite diario de " + MAX_DAILY_RESENAS + " reseñas");
            }
        }
    }
}

