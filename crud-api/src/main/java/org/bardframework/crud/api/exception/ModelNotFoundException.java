package org.bardframework.crud.api.exception;

public class ModelNotFoundException extends Exception {
    public ModelNotFoundException() {
    }

    public ModelNotFoundException(String message) {
        super(message);
    }
}