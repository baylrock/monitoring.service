package com.baylrock.monitor.reporter.rest.controllers;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.baylrock.monitor.reporter.Localiser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private final Localiser localiser;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestError unhandledException(Exception ex) {
        log.error("Unhandled exception occurred in rest controller", ex);
        return new RestError(localiser.localise("rest.errors.unexpected_server_internal"));
    }

    @ExceptionHandler({ URISyntaxException.class, MalformedURLException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestError uriExceptionHandler(Exception ex) {
        return new RestError(localiser.localise("rest.errors.requestdata.malformed_url", ex.getMessage()));
    }

    /**
     * Error message wrapper. Unifies the error model structure
     */
    @AllArgsConstructor
    @Getter
    public class RestError {
        private String error;
    }
}
