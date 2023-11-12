package com.example.iso8583.iso8583;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.ISO93BPackager;

public record ISO8583BalanceInquiryDTO(
        String acquirerInstitutionId,
        String operatorId,
        String accountNumber,
        String pointOfServiceDataCode,
        String referenceNumber,
        String countryCode,
        String currencyCode
) {
  public ISO8583BalanceInquiryDTO {
    if (referenceNumber == null || referenceNumber.isBlank() || referenceNumber.isEmpty()) {
      referenceNumber = ISOUtil.getRandomDigits(new Random(), 12, 10);
    }
    List<String> errors = new ArrayList<>();
    if (countryCode == null || countryCode.isBlank() || countryCode.isEmpty()) {
      errors.add("Country code is required");
    }
    if (currencyCode == null || currencyCode.isBlank() || currencyCode.isEmpty()) {
      errors.add("Currency is required");
    }
    if (accountNumber == null || accountNumber.isBlank() || accountNumber.isEmpty()) {
      errors.add("Account number is required");
    }
    if (operatorId == null || operatorId.isBlank() || operatorId.isEmpty()) {
      errors.add("operator ID is required");
    }
    if (!errors.isEmpty()) throw new IllegalArgumentException(String.join(". ", errors));
  }


  public ISOMsg buildISO8583Message(ISO93BPackager isoPacker) throws ISOException {
    Date currentDate = new Date();
    ISOMsg isoMsg = new ISOMsg();
    isoMsg.setPackager(isoPacker);
    isoMsg.setHeader(ISO8583Helper.buildHeader("02"));
    isoMsg.setMTI(ISO8583MessageType.FINANCIAL_TRANSACTION_REQUEST);
    isoMsg.set(2, operatorId + accountNumber);
    isoMsg.set(3, ISO8583TransactionType.BALANCE_INQUIRY);
    isoMsg.set(4, "0");
    isoMsg.set(7, new SimpleDateFormat("MMddhhmmss").format(currentDate));
    isoMsg.set(11, ISOUtil.getRandomDigits(new Random(), 6, 10));
    isoMsg.set(12, new SimpleDateFormat("yyMMddhhmmss").format(currentDate));
    isoMsg.set(17, new SimpleDateFormat("MMdd").format(currentDate));
    isoMsg.set(19, countryCode());
    isoMsg.set(22, pointOfServiceDataCode);
    isoMsg.set(24, ISO8583FunctionCode.ORIGINAL_FINANCIAL);
    isoMsg.set(26, "5271");
    isoMsg.set(28, new SimpleDateFormat("yyMMdd").format(currentDate));
    isoMsg.set(32, acquirerInstitutionId);
    isoMsg.set(37, referenceNumber);
    isoMsg.set(49, currencyCode);
    // isoMsg.set(102, acquirerInstitutionId + accountNumber);
    isoMsg.set(103, operatorId + accountNumber);
    return isoMsg;
  }
}
