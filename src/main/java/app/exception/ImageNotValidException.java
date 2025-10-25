package app.exception;

import app.exception.common.BaseException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageNotValidException extends BaseException{

    public ImageNotValidException(String redirectTo, String message) {
        super(message, redirectTo);
    }
    
}
