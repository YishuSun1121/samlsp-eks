package com.nextjump.saml2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AppException extends RuntimeException {

    private static final long serialVersionUID = 1l;

    private static final Map<String, HttpStatus> exceptionAndHttpStatusMap = new ConcurrentHashMap<>();

    private ExceptionCode exceptionCode;
    private Object[] arguments;
    private String message;

    private AppException() {
        super();
    }

    //only message
    private AppException(String message) {
        super();
        this.message = message;
    }

    //no message
    public AppException(ExceptionCode exceptionCode) {
        super(exceptionCode.toString());
        this.exceptionCode = exceptionCode;
    }

    public AppException(ExceptionCode exceptionCode, Object... arguments) {
        super(exceptionCode.toString());
        this.exceptionCode = exceptionCode;
        this.arguments = arguments;
    }

    public AppException(Throwable cause, ExceptionCode exceptionCode) {
        super(exceptionCode.toString(), cause);
        this.exceptionCode = exceptionCode;
    }

    public AppException(Throwable cause, ExceptionCode exceptionCode, Object... arguments) {
        super(exceptionCode.toString(), cause);
        this.exceptionCode = exceptionCode;
        this.arguments = arguments;
    }

    //contains message
    public AppException(ExceptionCode exceptionCode, String message) {
        super(exceptionCode.toString());
        this.exceptionCode = exceptionCode;
        this.message = message;
    }

    public AppException(ExceptionCode exceptionCode, String message, Object... arguments) {
        super(exceptionCode.toString());
        this.exceptionCode = exceptionCode;
        this.arguments = arguments;
        this.message = message;
    }

    public AppException(Throwable cause, ExceptionCode exceptionCode, String message) {
        super(exceptionCode.toString(), cause);
        this.exceptionCode = exceptionCode;
        this.message = message;
    }

    public AppException(Throwable cause, ExceptionCode exceptionCode, String message, Object... arguments) {
        super(exceptionCode.toString(), cause);
        this.exceptionCode = exceptionCode;
        this.arguments = arguments;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        String className = getClass().getName();
        HttpStatus httpStatus = exceptionAndHttpStatusMap.get(className);
        if (httpStatus == null) {
            ResponseStatus responseStatus = getClass().getAnnotation(ResponseStatus.class);
            httpStatus = (responseStatus != null) ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;
            exceptionAndHttpStatusMap.putIfAbsent(className, httpStatus);
        }
        return httpStatus;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public ExceptionCode getExceptionCode() {
        return exceptionCode;
    }

    @Override
    public String getMessage() {
        if (this.message != null) {
            return this.message;
        }
        return null;
    }

}
