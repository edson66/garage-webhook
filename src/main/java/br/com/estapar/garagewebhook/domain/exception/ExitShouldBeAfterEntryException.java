package br.com.estapar.garagewebhook.domain.exception;

public class ExitShouldBeAfterEntryException extends RuntimeException {
    public ExitShouldBeAfterEntryException(String s) {
        super(s);
    }
}
