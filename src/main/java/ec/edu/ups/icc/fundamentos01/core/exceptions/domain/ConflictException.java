package ec.edu.ups.icc.fundamentos01.core.exceptions.domain;

import org.springframework.http.HttpStatus;

import ec.edu.ups.icc.fundamentos01.core.exceptions.base.ApplicationException;

public class ConflictException extends ApplicationException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}