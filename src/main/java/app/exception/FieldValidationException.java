package app.exception;

import org.springframework.validation.BindingResult;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class FieldValidationException extends RuntimeException {
    
    private BindingResult bindResult;
    private String redirectTo;

    public FieldValidationException(String message, BindingResult result, String redirectTo) {
        super(message);
        this.bindResult = result;
        this.redirectTo = redirectTo;
    }


}
