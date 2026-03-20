package br.com.estapar.garagewebhook.infra.exception;

import br.com.estapar.garagewebhook.domain.exception.GarageFullException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> error404(EntityNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ValidationErrorData>> validationError(MethodArgumentNotValidException ex){
        var error = ex.getFieldErrors();

        return ResponseEntity.badRequest().body(error.stream().map(ValidationErrorData::new).toList());
    }

    @ExceptionHandler(GarageFullException.class)
    public ResponseEntity<String> garageFull(GarageFullException ex){
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericError(Exception ex) {
        return ResponseEntity.internalServerError().body("Erro interno no servidor: " + ex.getLocalizedMessage());
    }

    private record ValidationErrorData(String field,String message){

        public ValidationErrorData(FieldError fieldError) {
            this(fieldError.getField(), fieldError.getDefaultMessage());
        }
    }
}
