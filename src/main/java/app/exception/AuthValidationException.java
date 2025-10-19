package app.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthValidationException extends RuntimeException{
    private final HttpStatus status;        
    private final Object details;  
    private final String redirectTo;
    
    public AuthValidationException(String message, String redirectTo) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.details = null;
        this.redirectTo =redirectTo;
    }
    public AuthValidationException(String message, String redirectTo, Object details) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.details = details;
        this.redirectTo =redirectTo;
    }

    public AuthValidationException(String message, HttpStatus httpStatus, String redirectTo) {
        super(message);
        this.status = httpStatus;
        this.details = null;
        this.redirectTo = redirectTo;
    }

    public AuthValidationException(String message, HttpStatus httpStatus, Object details, String redirect) {
        super(message);
        this.status = httpStatus;
        this.details = details;
        this.redirectTo = redirect;
    }
}

