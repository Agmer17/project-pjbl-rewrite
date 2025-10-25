package app.exception.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseException extends RuntimeException {

    private String redirectTo;


    public BaseException(String message, String redirectTo) {
        super(message);
        this.redirectTo = redirectTo;
    }
    
}
