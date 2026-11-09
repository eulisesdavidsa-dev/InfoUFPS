package org.infoUFPS.Exceptions;

public class DailyLimitExceededException extends DomainException {
    public DailyLimitExceededException(String message) {
        super(message);
    }
}

