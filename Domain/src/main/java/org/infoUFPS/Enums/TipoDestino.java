package org.infoUFPS.Enums;

public enum TipoDestino {
    PROFESOR,
    MATERIA;

    public static TipoDestino fromString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (TipoDestino t : values()) {
            if (t.name().equalsIgnoreCase(text.trim())) {
                return t;
            }
        }
        throw new IllegalArgumentException("Tipo de destino desconocido: " + text);
    }
}

