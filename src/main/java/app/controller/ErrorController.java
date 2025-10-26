package app.controller;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.exception.DataNotFoundEx;
import app.exception.AuthValidationException;
import app.exception.FieldValidationException;
import app.exception.ImageNotValidException;

@ControllerAdvice
public class ErrorController {

    @ExceptionHandler(AuthValidationException.class)
    public String authValidationHandler(AuthValidationException ex, RedirectAttributes redAttrs) {

        redAttrs.addFlashAttribute("formRequest", ex.getDetails());
        redAttrs.addFlashAttribute("errorMessage", ex.getMessage());

        return "redirect:"+ex.getRedirectTo();


    }

    @ExceptionHandler(FieldValidationException.class)
    public String fieldValidationHandler(FieldValidationException ex, RedirectAttributes redAttrs) {

        Map<String, String> errors = null;

        if (ex.getBindResult() != null) {
            errors = ex.getBindResult().getFieldErrors().stream()
                        .collect(Collectors.toMap(
                                FieldError::getField,
                                FieldError::getDefaultMessage,
                                (msg1, _) -> msg1));
                redAttrs.addFlashAttribute("validationErrors", errors);
                redAttrs.addFlashAttribute("errorMessage", ex.getMessage());
                redAttrs.addFlashAttribute("formRequest", ex.getBindResult().getTarget());
                
            }
            return "redirect:"+ex.getRedirectTo();
        

    }

    @ExceptionHandler(ImageNotValidException.class)
    public String imageFormatNotValid(ImageNotValidException ex, RedirectAttributes redAttrs) {

        redAttrs.addFlashAttribute("errorMessage", ex.getMessage());

        return "redirect:"+ex.getRedirectTo();
    }

    @ExceptionHandler(DataNotFoundEx.class)
    public String AccountNotFound(DataNotFoundEx ex, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());


        return "redirect:"+ex.getRedirectTo();
    }
    
}
