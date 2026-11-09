package org.infoUFPS.Features.Resena.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoUFPS.DTOs.Requests.CreateResenaCommand;
import org.infoUFPS.DTOs.ResenaDto;
import org.infoUFPS.Entities.Account;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Entities.Profesor;
import org.infoUFPS.Entities.Resena;
import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Enums.TipoDestino;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Exceptions.DuplicateResourceException;
import org.infoUFPS.Exceptions.ResourceNotFoundException;
import org.infoUFPS.Repositories.IAccountRepository;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.Repositories.IProfesorRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.Services.DailyLimitService;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.MateriaId;
import org.infoUFPS.ValueObjects.ProfesorId;

@ApplicationScoped
public class CreateResenaHandler {

    private final IResenaRepository resenaRepository;
    private final IProfesorRepository profesorRepository;
    private final IMateriaRepository materiaRepository;
    private final IAccountRepository accountRepository;
    private final DailyLimitService dailyLimitService;

    public CreateResenaHandler() {
        this.resenaRepository = null;
        this.profesorRepository = null;
        this.materiaRepository = null;
        this.accountRepository = null;
        this.dailyLimitService = null;
    }

    @Inject
    public CreateResenaHandler(IResenaRepository resenaRepository,
                               IProfesorRepository profesorRepository,
                               IMateriaRepository materiaRepository,
                               IAccountRepository accountRepository,
                               DailyLimitService dailyLimitService) {
        this.resenaRepository = resenaRepository;
        this.profesorRepository = profesorRepository;
        this.materiaRepository = materiaRepository;
        this.accountRepository = accountRepository;
        this.dailyLimitService = dailyLimitService;
    }

    @Transactional
    public ResenaDto execute(CreateResenaCommand command) {
        if (command.autorId() == null || command.autorId().isBlank()) {
            throw new DomainException("El ID del autor es obligatorio");
        }
        AccountId autorId = AccountId.of(command.autorId());

        Account account = accountRepository.findById(autorId)
                .orElseThrow(() -> new DomainException("El usuario autor no existe"));

        if (dailyLimitService != null) {
            dailyLimitService.checkResenaLimit(autorId);
        }

        if (command.tipoDestino() == null) {
            throw new DomainException("El tipo de destino (PROFESOR o MATERIA) es obligatorio");
        }

        Resena resena;
        if (command.tipoDestino() == TipoDestino.PROFESOR) {
            if (command.profesorId() == null || command.profesorId().isBlank()) {
                throw new DomainException("El profesorId es obligatorio para una reseña a profesor");
            }
            ProfesorId profesorId = ProfesorId.of(command.profesorId());
            Profesor profesor = profesorRepository.findById(profesorId)
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontró el profesor con ID: " + command.profesorId()));

            if (profesor.getEstado() != EstadoModeracion.PUBLICADO) {
                throw new DomainException("No se pueden crear reseñas a profesores que no estén publicados");
            }

            if (resenaRepository.findActiveByUserAndProfesor(autorId, profesorId).isPresent()) {
                throw new DuplicateResourceException("Ya tienes una reseña activa (en revisión o publicada) para este profesor");
            }

            resena = Resena.createForProfesor(
                    autorId,
                    profesorId,
                    command.calificacion(),
                    command.comentario(),
                    command.materiaCursada()
            );
        } else if (command.tipoDestino() == TipoDestino.MATERIA) {
            if (command.materiaId() == null || command.materiaId().isBlank()) {
                throw new DomainException("El materiaId es obligatorio para una reseña a materia");
            }
            MateriaId materiaId = MateriaId.of(command.materiaId());
            Materia materia = materiaRepository.findById(materiaId)
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontró la materia con ID: " + command.materiaId()));

            if (materia.getEstado() != EstadoModeracion.PUBLICADO) {
                throw new DomainException("No se pueden crear reseñas a materias que no estén publicadas");
            }

            if (resenaRepository.findActiveByUserAndMateria(autorId, materiaId).isPresent()) {
                throw new DuplicateResourceException("Ya tienes una reseña activa (en revisión o publicada) para esta materia");
            }

            resena = Resena.createForMateria(
                    autorId,
                    materiaId,
                    command.calificacion(),
                    command.comentario()
            );
        } else {
            throw new DomainException("Tipo de destino no soportado: " + command.tipoDestino());
        }

        resenaRepository.save(resena);

        return ResenaDto.of(resena, account.getName());
    }
}

