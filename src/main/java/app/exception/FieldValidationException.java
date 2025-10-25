package app.exception;

import org.springframework.validation.BindingResult;

import app.exception.common.BaseException;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class FieldValidationException extends BaseException {
    
    private BindingResult bindResult;

    public FieldValidationException(String message, BindingResult result, String redirectTo) {
        super(message, redirectTo);
        this.bindResult = result;
    }


}
