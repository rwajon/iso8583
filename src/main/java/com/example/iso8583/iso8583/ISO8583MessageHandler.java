package com.example.iso8583.iso8583;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO93BPackager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ISO8583MessageHandler {
  private final ISO93BPackager isoPacker;

  @Value("${iso8583.issuerInstitutionId}")
  private String issuerInstitutionId;
  @Value("${iso8583.acquirerInstitutionId}")
  private String acquirerInstitutionId;

  public ISO8583MessageHandler(ISO8583Packer isoPacker) {
    this.isoPacker = isoPacker;
  }

  public ISOMsg handleNetworkMessages(ISOMsg isoMsg) throws ISOException {
    ISOMsg response = new ISOMsg();
    response.setPackager(this.isoPacker);
    response.setMTI(ISO8583MessageType.NETWORK_MANAGEMENT_RESPONSE);
    response.setHeader(ISO8583Helper.buildHeader("00"));

    for (var key : isoMsg.getChildren().keySet()) {
      if ((int) key >= 2) {
        response.set((int) key, isoMsg.getValue((int) key).toString());
      }
    }
    response.set(39, "800");
    isoMsg.set(93, issuerInstitutionId);
    isoMsg.set(94, acquirerInstitutionId);
    return response;
  }

  // TODO: implement
  public ISOMsg handleFinancialMessages(ISOMsg isoMsg) throws ISOException {
    ISOMsg response = new ISOMsg();
    response.setPackager(this.isoPacker);
    for (var key : isoMsg.getChildren().keySet()) {
      if ((int) key >= 2) {
        response.set((int) key, isoMsg.getValue((int) key).toString());
      }
    }
    response.setMTI(ISO8583MessageType.FINANCIAL_RESPONSE);
    response.setHeader(ISO8583Helper.buildHeader());
    response.set(39, "000");
    response.set(100, acquirerInstitutionId);
    return response;
  }
}
