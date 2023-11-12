package com.example.iso8583.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
@ResponseBody
public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) {
    super(message);
  }
}
