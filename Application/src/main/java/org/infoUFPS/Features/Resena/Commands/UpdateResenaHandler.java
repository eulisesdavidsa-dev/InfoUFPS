package org.infoUFPS.Features.Resena.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoUFPS.DTOs.Requests.UpdateResenaCommand;
import org.infoUFPS.DTOs.ResenaDto;
import org.infoUFPS.Entities.Account;
import org.infoUFPS.Entities.Resena;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Exceptions.ResourceNotFoundException;
import org.infoUFPS.Repositories.IAccountRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.ResenaId;

@ApplicationScoped
public class UpdateResenaHandler {

    private final IResenaRepository resenaRepository;
    private final IAccountRepository accountRepository;

    public UpdateResenaHandler() {
        this.resenaRepository = null;
        this.accountRepository = null;
    }

    @Inject
    public UpdateResenaHandler(IResenaRepository resenaRepository, IAccountRepository accountRepository) {
        this.resenaRepository = resenaRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public ResenaDto execute(UpdateResenaCommand command) {
        ResenaId resenaId = ResenaId.of(command.resenaId());
        AccountId autorId = AccountId.of(command.autorId());

        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la reseña con ID: " + command.resenaId()));

        if (!resena.getAutorId().equals(autorId)) {
            throw new DomainException("Solo el autor puede editar su reseña");
        }

        resena.updateByAuthor(command.calificacion(), command.comentario(), command.materiaCursada(), autorId);
        resenaRepository.save(resena);

        String autorNombre = accountRepository.findById(autorId).map(Account::getName).orElse("Usuario");

        return ResenaDto.of(resena, autorNombre);
    }
}

