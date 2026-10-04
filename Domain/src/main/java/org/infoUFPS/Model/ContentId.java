package org.infoUFPS.Model;

import org.infoUFPS.Exception.DomainException;

import java.util.UUID;

public final class ContentId {

    private final String value;

    private ContentId(String value) {
        if(value == null || value.isBlank())
            throw new DomainException("El contentID no puede estar vacio");
        this.value = value.strip();
        //Strip elimina espacios en blanco de un String
    }

    public static ContentId generate() {
        return new ContentId(UUID.randomUUID().toString());
        //Generador de randomUUID utilizando la clase UUID incluida en java
    }

    public static ContentId of (String value) {
        return new ContentId((value));
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContentId)) return false;
        ContentId that = (ContentId) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
