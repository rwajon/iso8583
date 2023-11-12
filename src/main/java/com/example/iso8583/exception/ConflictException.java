package com.example.iso8583.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
@ResponseBody
public class ConflictException extends RuntimeException {
  public ConflictException(String message) {
    super(message);
  }
}
