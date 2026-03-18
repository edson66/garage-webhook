package br.com.estapar.garagewebhook.domain.exception;

public class GarageFullException extends RuntimeException{
    public GarageFullException(String message){
        super(message);
    }
}
