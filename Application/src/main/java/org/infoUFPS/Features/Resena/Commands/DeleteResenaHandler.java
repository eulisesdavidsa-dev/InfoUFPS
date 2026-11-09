package org.infoUFPS.Features.Resena.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoUFPS.Entities.Resena;
import org.infoUFPS.Exceptions.DomainException;
import org.infoUFPS.Exceptions.ResourceNotFoundException;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.ValueObjects.AccountId;
import org.infoUFPS.ValueObjects.ResenaId;

@ApplicationScoped
public class DeleteResenaHandler {

    private final IResenaRepository resenaRepository;

    public DeleteResenaHandler() {
        this.resenaRepository = null;
    }

    @Inject
    public DeleteResenaHandler(IResenaRepository resenaRepository) {
        this.resenaRepository = resenaRepository;
    }

    @Transactional
    public void execute(String resenaIdStr, String requesterIdStr, boolean isAdmin) {
        ResenaId resenaId = ResenaId.of(resenaIdStr);
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la reseña con ID: " + resenaIdStr));

        AccountId requesterId = AccountId.of(requesterIdStr);
        if (!isAdmin && !resena.getAutorId().equals(requesterId)) {
            throw new DomainException("No tienes permiso para eliminar esta reseña");
        }

        resena.archive();
        resenaRepository.save(resena);
    }
}

