package com.example.iso8583.iso8583;

import java.util.Map;
import java.util.Objects;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO93BPackager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.iso8583.exception.NotFoundException;

@Service
public class ISO8583Service {
  private final ISO93BPackager isoPacker;
  private final ISO8583Client iso8583Client;
  @Value("${iso8583.issuerInstitutionId}")
  private String issuerInstitutionId;
  @Value("${iso8583.acquirerInstitutionId}")
  private String acquirerInstitutionId;

  public ISO8583Service(ISO8583Packer isoPacker, ISO8583Client iso8583Client) {
    this.isoPacker = isoPacker;
    this.iso8583Client = iso8583Client;
  }

  Map<String, Map<String, Map<String, String>>> signOn() throws ISOException {
    ISOMsg isoMsg = ISO8583Helper.buildSignOnMessage(isoPacker, issuerInstitutionId, acquirerInstitutionId);
    byte[] isoMsgBytes = iso8583Client.sendMessage(isoMsg, "Outgoing iso8583 sign-on message:")
                                      .awaitUninterruptibly()
                                      .resultNow();
    var request = ISO8583Helper.toMap(isoPacker, isoMsg.pack());
    var response = ISO8583Helper.toMap(isoPacker, isoMsgBytes);
    return Map.of("request", request, "response", response);
  }

  Map<String, Map<String, Map<String, String>>> echo() throws ISOException {
    ISOMsg isoMsg = ISO8583Helper.buildEchoMessage(isoPacker, issuerInstitutionId, acquirerInstitutionId);
    byte[] isoMsgBytes = iso8583Client.sendMessage(isoMsg, "Outgoing iso8583 echo message:")
                                      .awaitUninterruptibly()
                                      .resultNow();
    var request = ISO8583Helper.toMap(isoPacker, isoMsg.pack());
    var response = ISO8583Helper.toMap(isoPacker, isoMsgBytes);
    return Map.of("request", request, "response", response);
  }

  ISO8583ResponseDTO checkAccount(ISO8583BalanceInquiryDTO payload) throws ISOException {
    var iso8583BalanceInquiry = new ISO8583BalanceInquiryDTO(
            acquirerInstitutionId,
            payload.operatorId(),
            payload.accountNumber(),
            payload.pointOfServiceDataCode(),
            payload.referenceNumber(),
            payload.countryCode(),
            payload.currencyCode()
    );
    ISOMsg isoMsg = iso8583BalanceInquiry.buildISO8583Message(isoPacker);
    byte[] isoMsgBytes = iso8583Client.sendMessage(isoMsg, "Outgoing iso8583 checkAccount message:")
                                      .awaitUninterruptibly()
                                      .resultNow();
    var response = ISO8583Helper.bytesToISOMsg(isoPacker, isoMsgBytes);

    if (response.getMTI() == null ||
        response.getMTI().startsWith("9") ||
        response.getValue(39) == null ||
        !Objects.equals(response.getValue(39).toString(), "000")) {
      throw new NotFoundException("Account not found. STAN: " + response.getValue(11));
    }

    return new ISO8583ResponseDTO(isoMsg, response);
  }
}
