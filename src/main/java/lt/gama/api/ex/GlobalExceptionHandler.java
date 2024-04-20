package lt.gama.api.ex;

import lt.gama.api.APIResult;
import lt.gama.service.ex.GamaServerErrorException;
import lt.gama.service.ex.rt.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(GamaApiException.class)
    protected ResponseEntity<Object> handleGamaApiException(GamaApiException ex, WebRequest request) {
        return handleExceptionInternal(ex, APIResult.Error(ex.getMessage(), ex.getStatusCode()),
                new HttpHeaders(), HttpStatusCode.valueOf(ex.getStatusCode()), request);
    }

    @ExceptionHandler(GamaUnauthorizedException.class)
    protected ResponseEntity<Object> handleGamaUnauthorizedException(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, APIResult.Error("Unauthorized", HttpStatus.UNAUTHORIZED.value()), new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(GamaForbiddenException.class)
    protected ResponseEntity<Object> handleGamaForbiddenException(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, APIResult.Error("Forbidden", HttpStatus.FORBIDDEN.value()), new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(GamaNotFoundException.class)
    protected ResponseEntity<Object> handleGamaNotFoundException(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, APIResult.Error(ex.getMessage(), HttpStatus.NOT_FOUND.value()),
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(GamaServerErrorException.class)
    protected ResponseEntity<Object> handleGamaServerErrorException(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, APIResult.Error("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value()),
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleBadRequestException(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, APIResult.Error(ex.getMessage(), HttpStatus.BAD_REQUEST.value()),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

//    record RestErrorResponse(int status, String message, LocalDateTime timestamp) {
//        RestErrorResponse(HttpStatus status, String message) {
//            this(status.value(), message, LocalDateTime.now());
//        }
//        RestErrorResponse(int status, String message) {
//            this(status, message, LocalDateTime.now());
//        }
//        RestErrorResponse(HttpStatus status) {
//            this(status.value(), null, LocalDateTime.now());
//        }
//    }
}
