package org.bardframework.crud.exception;

import lombok.Getter;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.SimpleErrors;

/**
 * Created by v.zafari on 1/26/2016.
 */
@Getter
public class InvalidFieldException extends RuntimeException {

    private final transient Errors errors;

    public InvalidFieldException(Errors errors) {
        this.errors = errors;
    }

    public InvalidFieldException(String field) {
        Errors errors = new SimpleErrors("fields");
        errors.getFieldErrors().add(new FieldError("fields", "fields", "The '%s' field is not valid for filtering the output results".formatted(field)));
        this.errors = errors;
    }

    public InvalidFieldException rejectValue(String field, String errorCode, String defaultMessage, Object... errorArgs) {
        this.errors.rejectValue(field, errorCode, errorArgs, defaultMessage);
        return this;
    }

}
