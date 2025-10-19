package app.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageNotValidException extends RuntimeException{

    private String redirectTo;

    public ImageNotValidException(String msg, String redirect) {
        super(msg);
        this.redirectTo = redirect;
    }
    
}
