package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;   // <-- fehlt sonst
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


/**
 * Translates backend Exceptions to HTTP responses with proper status codes and payloads.
 * Logs each exception exactly once at an appropriate level (TS 10).
 */
@RestControllerAdvice
public class ApplicationExceptionHandler {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /** 422 – Validation errors with field list (US/TS15). */
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler(ValidationException.class)
  public ValidationErrorRestDto handleValidation(ValidationException e) {
    LOG.warn("422 Unprocessable Entity: {}. Failed validations: {}.",
            e.summary(), String.join(", ", e.errors()));

    return new ValidationErrorRestDto(e.summary(), e.errors());
  }


  /** 404 – Referenzierte Ressource nicht gefunden (Eltern/Owner/Pferd). */
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFound(NotFoundException e) {
    LOG.warn("404 Not Found: {}", e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** 409 – Konflikte (z.B. FK-Konflikt beim Löschen). */
  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(ConflictException.class)
  public ProblemDetail handleConflict(ConflictException e) {
    LOG.warn("409 Conflict: {}", e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
  }

  /** 400 – Ungültiger Request (kaputtes JSON, falscher Typ/Format). */
  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    MethodArgumentNotValidException.class
  })
  public ProblemDetail handleBadRequest(Exception e) {
    LOG.warn("400 Bad Request: {}", e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed or invalid request");
  }

  /** 500 – Unerwarteter Fehler. */
  @ExceptionHandler(FatalException.class)
  public ProblemDetail handleFatal(FatalException e) {
    LOG.error("500 Internal Server Error: {}", e.getMessage(), e);
    return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
  }

  /** Fallback – alles andere. */
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleAny(Exception e) {
    LOG.error("Unhandled exception -> 500: {}", e.getMessage(), e);
    return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
  }
}
