package com.example.iso8583.iso8583;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import com.example.iso8583.util.Logger;

public class ISO8583Helper {
  private static final Logger logger = new Logger(ISO8583Helper.class);

  public ISO8583Helper() {}

  public static byte[] buildHeader() {
    return buildHeader("02");
  }

  public static byte[] buildHeader(String messageSource) {
    try {
      ByteArrayOutputStream header = new ByteArrayOutputStream();
      header.write(("ISO8583-1993" + messageSource + "1000000").getBytes(StandardCharsets.UTF_8));
      return header.toByteArray();
    } catch (IOException e) {
      return "ISO8583-1993021000000".getBytes(StandardCharsets.UTF_8);
    }
  }

  public static ISOMsg buildEchoMessage(ISOBasePackager isoPacker, String issuerInstitutionId,
                                        String acquirerInstitutionId) throws ISOException {
    Date currentDate = new Date();
    ISOMsg isoMsg = new ISOMsg();
    isoMsg.setPackager(isoPacker);
    isoMsg.setHeader(ISO8583Helper.buildHeader("00"));
    isoMsg.setMTI(ISO8583MessageType.NETWORK_MANAGEMENT_REQUEST);
    isoMsg.set(7, new SimpleDateFormat("MMddhhmmss").format(currentDate));
    isoMsg.set(11, ISOUtil.getRandomDigits(new Random(), 6, 10));
    isoMsg.set(24, ISO8583FunctionCode.ECHO);
    isoMsg.set(28, new SimpleDateFormat("yyMMdd").format(currentDate));
    isoMsg.set(93, issuerInstitutionId);
    isoMsg.set(94, acquirerInstitutionId);
    return isoMsg;
  }

  public static ISOMsg buildSignOnMessage(ISOBasePackager isoPacker, String issuerInstitutionId,
                                          String acquirerInstitutionId) throws ISOException {
    Date currentDate = new Date();
    ISOMsg isoMsg = new ISOMsg();
    isoMsg.setPackager(isoPacker);
    isoMsg.setHeader(ISO8583Helper.buildHeader("00"));
    isoMsg.setMTI(ISO8583MessageType.NETWORK_MANAGEMENT_REQUEST);
    isoMsg.set(7, new SimpleDateFormat("MMddhhmmss").format(currentDate));
    isoMsg.set(11, ISOUtil.getRandomDigits(new Random(), 6, 10));
    isoMsg.set(24, ISO8583FunctionCode.LOGON);
    isoMsg.set(28, new SimpleDateFormat("yyMMdd").format(currentDate));
    isoMsg.set(93, issuerInstitutionId);
    isoMsg.set(94, acquirerInstitutionId);
    return isoMsg;
  }

  public static ISOMsg bytesToISOMsg(ISOBasePackager isoPacker, byte[] isoMsgBytes) {
    ISOMsg isoMsg = new ISOMsg();
    isoMsg.setPackager(isoPacker);
    try {
      isoMsg.unpack(isoMsgBytes);
    } catch (ISOException ignored) {}
    return isoMsg;
  }

  public static Map<String, Map<String, String>> toMap(ISOBasePackager isoPacker, byte[] isoMsgBytes) {
    ISOMsg isoMsg = new ISOMsg();
    isoMsg.setPackager(isoPacker);
    try {
      isoMsg.unpack(isoMsgBytes);
    } catch (ISOException ignored) {}
    Map<String, Map<String, String>> result = new LinkedHashMap<>();
    result.put("message", Map.of("value", ISOUtil.byte2hex(isoMsgBytes), "description", "Message in hexadecimal"));
    result.putAll(toMap(isoPacker, isoMsg));
    return result;
  }

  public static Map<String, Map<String, String>> toMap(ISOBasePackager isoPacker, ISOMsg isoMsg) {
    Map<String, Map<String, String>> result = new LinkedHashMap<>();
    if (isoMsg.getHeader() != null) {
      result.put("Header", Map.of("value", new String(isoMsg.getHeader()), "description", "Message header"));
    }
    for (var key : isoMsg.getChildren().keySet()) {
      if ((int) key != -1) {
        String description = isoPacker.getFieldPackager((int) key).getDescription();
        String value = isoMsg.getValue((int) key).toString();
        result.put(key.toString(), Map.of("value", value, "description", description));
      }
    }
    return result;
  }

  public static void logMessage(ISOBasePackager isoPacker, ISOMsg isoMsg) {
    try {
      logMessage(isoPacker, isoMsg, isoMsg.pack());
    } catch (ISOException e) {
      logger.error("Failed to log message: %s".formatted(e.getMessage()));
    }
  }

  public static void logMessage(ISOBasePackager isoPacker, ISOMsg isoMsg, byte[] isoMsgBytes) {
    StringBuilder isoMsgLog = new StringBuilder();
    if (isoMsg.getHeader() != null) {
      isoMsgLog.append("Message header:......%s%n".formatted(new String(isoMsg.getHeader())));
    }
    for (var key : isoMsg.getChildren().keySet()) {
      if ((int) key != -1) {
        String description = isoPacker.getFieldPackager((int) key).getDescription();
        String value = isoMsg.getValue((int) key).toString();
        isoMsgLog.append("%s[%s]:%s%n".formatted(description, key, String.format("%20s", value).replace(' ', '.')));
      }
    }
    isoMsgLog.append(ISOUtil.hexdump(isoMsgBytes));
    logger.info(new String(isoMsgBytes, StandardCharsets.US_ASCII));
    logger.info(ISOUtil.byte2hex(isoMsgBytes));
    logger.info("%n%s".formatted(isoMsgLog.toString()));
    isoMsg.dump(System.out, "");
  }
}
