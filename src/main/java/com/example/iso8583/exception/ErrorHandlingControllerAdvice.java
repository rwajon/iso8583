package com.example.iso8583.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ErrorHandlingControllerAdvice {
  @ExceptionHandler
  ProblemDetail problemDetail(IllegalStateException e, HttpServletRequest request) {
    var pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
    request.getHeaderNames().asIterator().forEachRemaining(System.err::println);
    return pd;
  }

  @ExceptionHandler
  ProblemDetail problemDetail(IllegalArgumentException e, HttpServletRequest request) {
    var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
    request.getHeaderNames().asIterator().forEachRemaining(System.err::println);
    return pd;
  }

  @ExceptionHandler
  ProblemDetail problemDetail(BadRequestException e, HttpServletRequest request) {
    var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
    request.getHeaderNames().asIterator().forEachRemaining(System.err::println);
    return pd;
  }

  @ExceptionHandler
  ProblemDetail problemDetail(NotFoundException e, HttpServletRequest request) {
    var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getLocalizedMessage());
    request.getHeaderNames().asIterator().forEachRemaining(System.err::println);
    return pd;
  }

  @ExceptionHandler
  ProblemDetail problemDetail(ConflictException e, HttpServletRequest request) {
    var pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getLocalizedMessage());
    request.getHeaderNames().asIterator().forEachRemaining(System.err::println);
    return pd;
  }
}
