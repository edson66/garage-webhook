package br.com.estapar.garagewebhook.domain.exception;

public class NotParkedException extends RuntimeException {
    public NotParkedException(String message) {
        super(message);
    }
}
