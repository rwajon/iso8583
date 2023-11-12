package com.example.iso8583.iso8583;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jpos.iso.ISOException;
import org.jpos.iso.packager.ISO93BPackager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/iso8583")
public class ISO8583Controller {
  private final ISO93BPackager isoPacker;
  private final ISO8583Service iso8583Service;

  public ISO8583Controller(ISO8583Packer isoPacker, ISO8583Service iso8583Service) {
    this.isoPacker = isoPacker;
    this.iso8583Service = iso8583Service;
  }

  @GetMapping(value = {"/", "/sign-on"})
  ResponseEntity<Object> signOn() throws ISOException {
    Map<String, Map<String, Map<String, String>>> result = iso8583Service.signOn();
    var statusCode = result.get("response")
                           .get("0")
                           .get("value")
                           .startsWith("9") ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
    return ResponseEntity.status(statusCode).body(result);
  }

  @GetMapping(value = {"/", "/echo"})
  ResponseEntity<Object> echo() throws ISOException {
    Map<String, Map<String, Map<String, String>>> result = iso8583Service.echo();
    var statusCode = result.get("response")
                           .get("0")
                           .get("value")
                           .startsWith("9") ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
    return ResponseEntity.status(statusCode).body(result);
  }

  @PostMapping(value = {"/verify-account", "/check-account"})
  ResponseEntity<Object> checkAccount(@RequestBody ISO8583BalanceInquiryDTO body) throws ISOException {
    ISO8583ResponseDTO result = iso8583Service.checkAccount(body);
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("status", HttpStatus.OK.value());
    response.put("message", "Account found");
    response.put("isoRequest", ISO8583Helper.toMap(isoPacker, result.isoRequest()));
    response.put("isoResponse", ISO8583Helper.toMap(isoPacker, result.isoResponse()));
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
