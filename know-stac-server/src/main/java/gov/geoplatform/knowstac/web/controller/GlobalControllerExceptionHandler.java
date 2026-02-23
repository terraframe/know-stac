package gov.geoplatform.knowstac.web.controller;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import gov.geoplatform.knowstac.core.model.InvalidAccessException;

@RestControllerAdvice
public class GlobalControllerExceptionHandler
{

  @ExceptionHandler(ConversionFailedException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<String> handleConversion(RuntimeException ex)
  {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(InvalidAccessException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ResponseEntity<String> handleInvalidAccessException(RuntimeException ex)
  {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
  }
}