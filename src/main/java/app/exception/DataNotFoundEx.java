package app.exception;

import app.exception.common.BaseException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataNotFoundEx extends BaseException {


    public DataNotFoundEx(String message, String rediretTo) {
        super(message, rediretTo);
    }


    
}
