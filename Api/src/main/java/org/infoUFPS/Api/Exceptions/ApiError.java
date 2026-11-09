package org.infoUFPS.Api.Exceptions;

/**
 * Cuerpo JSON de error de la API.
 *
 * @param error   código corto del error.
 * @param message mensaje legible.
 */
public record ApiError(String error, String message) {
}
