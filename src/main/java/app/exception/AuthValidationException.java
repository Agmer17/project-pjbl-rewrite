package app.exception;

import org.springframework.http.HttpStatus;

import app.exception.common.BaseException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthValidationException extends BaseException{        
    private final Object details;  
    
    public AuthValidationException(String message, String redirectTo) {
        super(message, redirectTo);
        this.details = null;
    }
    public AuthValidationException(String message, String redirectTo, Object details) {
        super(message, redirectTo);
        this.details = details;
    }
}

