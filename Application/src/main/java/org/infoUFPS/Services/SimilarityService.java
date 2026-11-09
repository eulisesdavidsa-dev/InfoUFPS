package org.infoUFPS.Services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoUFPS.DTOs.ModeracionWarningDto;
import org.infoUFPS.Entities.Materia;
import org.infoUFPS.Entities.Profesor;
import org.infoUFPS.Enums.Facultad;
import org.infoUFPS.Repositories.IMateriaRepository;
import org.infoUFPS.Repositories.IProfesorRepository;
import org.infoUFPS.Utils.TextNormalizer;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class SimilarityService {

    private final IProfesorRepository profesorRepository;
    private final IMateriaRepository materiaRepository;

    public SimilarityService() {
        this.profesorRepository = null;
        this.materiaRepository = null;
    }

    @Inject
    public SimilarityService(IProfesorRepository profesorRepository, IMateriaRepository materiaRepository) {
        this.profesorRepository = profesorRepository;
        this.materiaRepository = materiaRepository;
    }

    public List<ModeracionWarningDto> checkProfesorSimilarity(String nombre, Facultad facultad) {
        List<ModeracionWarningDto> warnings = new ArrayList<>();
        if (profesorRepository == null || nombre == null || nombre.isBlank()) {
            return warnings;
        }

        String normalized = TextNormalizer.normalizeAndSortWords(nombre);
        List<Profesor> candidates = profesorRepository.findSimilar(normalized, facultad);

        for (Profesor p : candidates) {
            double sim = TextNormalizer.calculateSimilarity(nombre, p.getNombre());
            if (sim >= 0.70) {
                warnings.add(new ModeracionWarningDto(
                        p.getId().value(),
                        p.getNombre(),
                        p.getEstado().name(),
                        Math.round(sim * 100.0) / 100.0,
                        "Existe un profesor con nombre similar (" + (int)(sim * 100) + "% de similitud): '" + p.getNombre() + "'"
                ));
            }
        }
        return warnings;
    }

    public List<ModeracionWarningDto> checkMateriaSimilarity(String nombre) {
        List<ModeracionWarningDto> warnings = new ArrayList<>();
        if (materiaRepository == null || nombre == null || nombre.isBlank()) {
            return warnings;
        }

        String normalized = TextNormalizer.normalizeAndSortWords(nombre);
        List<Materia> candidates = materiaRepository.findSimilar(normalized);

        for (Materia m : candidates) {
            double sim = TextNormalizer.calculateSimilarity(nombre, m.getNombre());
            if (sim >= 0.70) {
                warnings.add(new ModeracionWarningDto(
                        m.getId().value(),
                        m.getNombre(),
                        m.getEstado().name(),
                        Math.round(sim * 100.0) / 100.0,
                        "Existe una materia con nombre similar (" + (int)(sim * 100) + "% de similitud): '" + m.getNombre() + "'"
                ));
            }
        }
        return warnings;
    }
}

