package org.infoUFPS.Features.Moderacion.Commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoUFPS.DTOs.MateriaDetailDto;
import org.infoUFPS.DTOs.Requests.ModerateEntityCommand;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Exceptions.DuplicateResourceException;
import org.infoUFPS.Exceptions.ResourceNotFoundException;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.Repositories.IResenaRepository;
import org.infoUFPS.ValueObjects.MateriaId;

@ApplicationScoped
public class ModerateMateriaHandler {

    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    public ModerateMateriaHandler() {
        this.materiaRepository = null;
        this.resenaRepository = null;
    }

    @Inject
    public ModerateMateriaHandler(IMateriaRepository materiaRepository, IResenaRepository resenaRepository) {
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    @Transactional
    public MateriaDetailDto publish(String id, ModerateEntityCommand command) {
        MateriaId materiaId = MateriaId.of(id);
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la materia con ID: " + id));

        if (command != null) {
            String nombreFinal = (command.nuevoNombre() != null && !command.nuevoNombre().isBlank())
                    ? command.nuevoNombre()
                    : materia.getNombre();
            String codigoFinal = (command.nuevoCodigo() != null && !command.nuevoCodigo().isBlank())
                    ? command.nuevoCodigo().strip().toUpperCase()
                    : materia.getCodigo();
            Integer creditosFinal = command.nuevosCreditos() != null ? command.nuevosCreditos() : materia.getCreditos();
            var tipoFinal = command.nuevoTipo() != null ? command.nuevoTipo() : materia.getTipo();

            if (codigoFinal != null && materiaRepository.existsByCodigoAndNotId(codigoFinal, materiaId)) {
                throw new DuplicateResourceException("Ya existe una materia registrada con el código: " + codigoFinal);
            }

            materia.adminUpdate(nombreFinal, codigoFinal, creditosFinal, tipoFinal);
        } else {
            if (materia.getCodigo() != null && materiaRepository.existsByCodigoAndNotId(materia.getCodigo(), materiaId)) {
                throw new DuplicateResourceException("Ya existe una materia registrada con el código: " + materia.getCodigo());
            }
        }

        materia.publish();
        materiaRepository.save(materia);

        Double avg = resenaRepository.getAverageRatingForMateria(materiaId);
        long count = resenaRepository.countPublishedByMateria(materiaId);

        return MateriaDetailDto.of(materia, avg, count);
    }

    @Transactional
    public MateriaDetailDto archive(String id) {
        MateriaId materiaId = MateriaId.of(id);
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la materia con ID: " + id));

        materia.archive();
        materiaRepository.save(materia);

        Double avg = resenaRepository.getAverageRatingForMateria(materiaId);
        long count = resenaRepository.countPublishedByMateria(materiaId);

        return MateriaDetailDto.of(materia, avg, count);
    }
}

