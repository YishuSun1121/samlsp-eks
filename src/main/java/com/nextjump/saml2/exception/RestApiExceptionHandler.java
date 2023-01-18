package com.nextjump.saml2.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

import javax.validation.ConstraintViolationException;
import java.io.IOException;

@ControllerAdvice
public class RestApiExceptionHandler {
    @ExceptionHandler(ConstraintViolationException.class)
    public void handleConstraintViolationException(ConstraintViolationException exception,
                                                   ServletWebRequest webRequest) throws IOException {
        writeErrorToResponse(exception, webRequest);
    }

    @ExceptionHandler(AppValidationException.class)
    public void handleAppException(AppException exception,
                                   ServletWebRequest webRequest) throws IOException {
        writeErrorToResponse(exception, webRequest);
    }

    private void writeErrorToResponse(Exception ex, ServletWebRequest webRequest) throws IOException {
        webRequest.getResponse().setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        webRequest.getResponse().setCharacterEncoding("UTF-8");
        webRequest.getResponse().setStatus(HttpStatus.BAD_REQUEST.value());
        webRequest.getResponse().getWriter().write("{\"timestamp\":" + System.currentTimeMillis() + ",\"status\":400," +
                "\"error\":\"Bad Request\",\"message\":\"" + ex.getMessage() + "\"}");
        webRequest.getResponse().flushBuffer();
    }
}
