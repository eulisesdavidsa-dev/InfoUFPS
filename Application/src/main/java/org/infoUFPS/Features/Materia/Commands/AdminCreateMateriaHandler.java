package org.infoUFPS.Features.Materia.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoUFPS.DTOs.MateriaDetailDto;
import org.infoUFPS.DTOs.Requests.AdminSaveMateriaCommand;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Exceptions.DuplicateResourceException;
import org.infoUFPS.Repositories.IMateriaRepository;

@ApplicationScoped
public class AdminCreateMateriaHandler {

    private final IMateriaRepository materiaRepository;

    public AdminCreateMateriaHandler() {
        this.materiaRepository = null;
    }

    @Inject
    public AdminCreateMateriaHandler(IMateriaRepository materiaRepository) {
        this.materiaRepository = materiaRepository;
    }

    @Transactional
    public MateriaDetailDto execute(AdminSaveMateriaCommand command) {
        if (command.codigo() != null && !command.codigo().isBlank()) {
            String cleanCode = command.codigo().strip().toUpperCase();
            if (materiaRepository.existsByCodigo(cleanCode)) {
                throw new DuplicateResourceException("Ya existe una materia registrada con el código: " + cleanCode);
            }
        }

        Materia materia = Materia.createByAdmin(
                command.nombre(),
                command.codigo(),
                command.creditos(),
                command.tipo()
        );

        materiaRepository.save(materia);

        return MateriaDetailDto.of(materia, null, 0L);
    }
}

