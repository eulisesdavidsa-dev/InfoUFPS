package org.infoUFPS.Features.Moderacion.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoUFPS.DTOs.ResenaDto;
import org.infoUFPS.Entities.Account;
import org.infoUFPS.Entities.Resena;
import org.infoUFPS.Exceptions.ResourceNotFoundException;
import org.infoUFPS.Repositories.IAccountRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.ValueObjects.ResenaId;

@ApplicationScoped
public class ModerateResenaHandler {

    private final IResenaRepository resenaRepository;
    private final IAccountRepository accountRepository;

    public ModerateResenaHandler() {
        this.resenaRepository = null;
        this.accountRepository = null;
    }

    @Inject
    public ModerateResenaHandler(IResenaRepository resenaRepository, IAccountRepository accountRepository) {
        this.resenaRepository = resenaRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public ResenaDto publish(String id) {
        ResenaId resenaId = ResenaId.of(id);
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la reseña con ID: " + id));

        resena.publish();
        resenaRepository.save(resena);

        String autorNombre = accountRepository.findById(resena.getAutorId()).map(Account::getName).orElse("Usuario");
        return ResenaDto.of(resena, autorNombre);
    }

    @Transactional
    public ResenaDto archive(String id) {
        ResenaId resenaId = ResenaId.of(id);
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la reseña con ID: " + id));

        resena.archive();
        resenaRepository.save(resena);

        String autorNombre = accountRepository.findById(resena.getAutorId()).map(Account::getName).orElse("Usuario");
        return ResenaDto.of(resena, autorNombre);
    }
}

