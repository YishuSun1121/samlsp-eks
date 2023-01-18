package com.nextjump.saml2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AppValidationException extends AppException {
    public AppValidationException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }

    public AppValidationException(ExceptionCode exceptionCode, Object... arguments) {
        super(exceptionCode, arguments);
    }

    public AppValidationException(Throwable cause, ExceptionCode exceptionCode) {
        super(cause, exceptionCode);
    }

    public AppValidationException(Throwable cause, ExceptionCode exceptionCode, Object... arguments) {
        super(cause, exceptionCode, arguments);
    }

    public AppValidationException(ExceptionCode exceptionCode, String message) {
        super(exceptionCode, message);
    }

    public AppValidationException(ExceptionCode exceptionCode, String message, Object... arguments) {
        super(exceptionCode, message, arguments);
    }

    public AppValidationException(Throwable cause, ExceptionCode exceptionCode, String message) {
        super(cause, exceptionCode, message);
    }

    public AppValidationException(Throwable cause, ExceptionCode exceptionCode, String message, Object... arguments) {
        super(cause, exceptionCode, message, arguments);
    }
}
