package com.example.iso8583.iso8583;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.jpos.iso.*;
import org.jpos.iso.packager.ISO93BPackager;
import org.springframework.stereotype.Component;
import com.example.iso8583.util.Byte;

@Component
public class ISO8583Packer extends ISO93BPackager {
  protected ISOFieldPackager[] fields = {
          new IFB_NUMERIC(/*0*/ 4, "Message Type Indicator", true),
          new IFB_BITMAP(/*1*/ 16, "Bitmap"),
          new IFB_LLHNUM(/*2*/ 19, "Primary Account number", false),
          new IFB_NUMERIC(/*3*/ 6, "Processing Code", true),
          new IFB_NUMERIC(/*4*/ 12, "Amount, Transaction", true),
          new IFB_NUMERIC(/*5*/ 12, "Amount, Reconciliation", true),
          new IFB_NUMERIC(/*6*/ 12, "Amount, Cardholder billing", true),
          new IFB_NUMERIC(/*7*/ 10, "Date and time, transmission", true),
          new IFB_NUMERIC(/*8*/ 8, "Amount, Cardholder billing fee", true),
          new IFB_NUMERIC(/*9*/ 8, "Conversion rate, Reconciliation", true),
          new IFB_NUMERIC(/*10*/ 8, "Conversion rate, Cardholder billing", true),
          new IFB_NUMERIC(/*11*/ 6, "Systems trace audit number", true),
          new IFB_NUMERIC(/*12*/ 12, "Date and time, Local transaction", true),
          new IFB_NUMERIC(/*13*/ 4, "Date, Effective", true),
          new IFB_NUMERIC(/*14*/ 4, "Date, Expiration", true),
          new IFB_NUMERIC(/*15*/ 6, "Date, Settlement", true),
          new IFB_NUMERIC(/*16*/ 4, "Date, Conversion", true),
          new IFB_NUMERIC(/*17*/ 4, "Date, Capture", true),
          new IFB_NUMERIC(/*18*/ 4, "Merchant type", true),
          new IFB_NUMERIC(/*19*/ 3, "Country code, Acquiring institution", true),
          new IFB_NUMERIC(/*20*/ 3, "Country code, Primary account number", true),
          new IFB_NUMERIC(/*21*/ 3, "Country code, Forwarding institution", true),
          new IF_CHAR(/*22*/ 12, "Point of service data code"),
          new IFB_NUMERIC(/*23*/ 3, "Card sequence number", true),
          new IFB_NUMERIC(/*24*/ 3, "Function code", true),
          new IFB_NUMERIC(/*25*/ 4, "Message reason code", true),
          new IFB_NUMERIC(/*26*/ 4, "Card acceptor business code", true),
          new IFB_NUMERIC(/*27*/ 1, "Approval code length", true),
          new IFB_NUMERIC(/*28*/ 6, "Date, Reconciliation", true),
          new IFB_NUMERIC(/*29*/ 3, "Reconciliation indicator", true),
          new IFB_NUMERIC(/*30*/ 24, "Amounts, original", true),
          new IFB_LLHCHAR(/*31*/ 99, "Acquirer reference data"),
          new IFB_LLHNUM(/*32*/ 11, "Acquirer institution ident code", false),
          new IFB_LLHNUM(/*33*/ 11, "Forwarding institution ident code", false),
          new IFB_LLHCHAR(/*34*/ 28, "Primary account number, extended"),
          new IFB_LLHCHAR(/*35*/ 37, "Track 2 data"),
          new IFB_LLLHCHAR(/*36*/ 104, "Track 3 data"),
          new IF_CHAR(/*37*/ 12, "Retrieval reference number"),
          new IF_CHAR(/*38*/ 6, "Approval code"),
          new IFB_NUMERIC(/*39*/ 3, "Action code", true),
          new IFB_NUMERIC(/*40*/ 3, "Service code", true),
          new IF_CHAR(/*41*/ 8, "Card acceptor terminal identification"),
          new IF_CHAR(/*42*/ 15, "Card acceptor identification code"),
          new IFB_LLHCHAR(/*43*/ 99, "Card acceptor name/location"),
          new IFB_LLHCHAR(/*44*/ 99, "Additional response data"),
          new IFB_LLHCHAR(/*45*/ 76, "Track 1 data"),
          new IFB_LLLHCHAR(/*46*/ 204, "Amounts, Fees"),
          new IFB_LLLHCHAR(/*47*/ 999, "Additional data - national"),
          new IFB_LLLHCHAR(/*48*/ 999, "Additional data - private"),
          new IFB_NUMERIC(/*49*/ 3, "Currency code, Transaction", true),
          new IFB_NUMERIC(/*50*/ 3, "Currency code, Reconciliation", true),
          new IFB_NUMERIC(/*51*/ 3, "Currency code, Cardholder billing", true),
          new IFB_BINARY(/*52*/ 8, "Personal identification number (PIN) data"),
          new IFB_LLHNUM(/*53*/ 48, "Security related control information", false),
          new IFB_LLLHCHAR(/*54*/ 120, "Amounts, additional"),
          new IFB_LLLHBINARY(/*55*/ 255, "IC card system related data"),
          new IFB_LLHNUM(/*56*/ 35, "Original data elements", false),
          new IFB_NUMERIC(/*57*/ 3, "Authorization life cycle code", true),
          new IFB_LLHNUM(/*58*/ 11, "Authorizing agent institution Id Code", false),
          new IFB_LLLHCHAR(/*59*/ 999, "Transport data"),
          new IFB_LLLHCHAR(/*60*/ 999, "Reserved for national use"),
          new IFB_LLLHCHAR(/*61*/ 999, "Reserved for national use"),
          new IFB_LLLHCHAR(/*62*/ 999, "Reserved for private use"),
          new IFB_LLLHCHAR(/*63*/ 999, "Reserved for private use"),
          new IFB_BINARY(/*64*/ 8, "Message authentication code field"),
          new IFB_BINARY(/*65*/ 8, "Reserved for ISO use"),
          new IFB_LLLHCHAR(/*66*/ 204, "Amounts, original fees"),
          new IFB_NUMERIC(/*67*/ 2, "Extended payment data", true),
          new IFB_NUMERIC(/*68*/ 3, "Country code, receiving institution", true),
          new IFB_NUMERIC(/*69*/ 3, "Country code, settlement institution", true),
          new IFB_NUMERIC(/*70*/ 3, "Country code, authorizing agent Inst.", true),
          new IFB_NUMERIC(/*71*/ 8, "Message number", true),
          new IFB_LLLHCHAR(/*72*/ 999, "Data record"),
          new IFB_NUMERIC(/*73*/ 6, "Date, action", true),
          new IFB_NUMERIC(/*74*/ 10, "Credits, number", true),
          new IFB_NUMERIC(/*75*/ 10, "Credits, reversal number", true),
          new IFB_NUMERIC(/*76*/ 10, "Debits, number", true),
          new IFB_NUMERIC(/*77*/ 10, "Debits, reversal number", true),
          new IFB_NUMERIC(/*78*/ 10, "Transfer, number", true),
          new IFB_NUMERIC(/*79*/ 10, "Transfer, reversal number", true),
          new IFB_NUMERIC(/*80*/ 10, "Inquiries, number", true),
          new IFB_NUMERIC(/*81*/ 10, "Authorizations, number", true),
          new IFB_NUMERIC(/*82*/ 10, "Inquiries, reversal number", true),
          new IFB_NUMERIC(/*83*/ 10, "Payments, number", true),
          new IFB_NUMERIC(/*84*/ 10, "Payments, reversal number", true),
          new IFB_NUMERIC(/*85*/ 10, "Fee collections, number", true),
          new IFB_NUMERIC(/*86*/ 16, "Credits, amount", true),
          new IFB_NUMERIC(/*87*/ 16, "Credits, reversal amount", true),
          new IFB_NUMERIC(/*88*/ 16, "Debits, amount", true),
          new IFB_NUMERIC(/*89*/ 16, "Debits, reversal amount", true),
          new IFB_NUMERIC(/*90*/ 10, "Authorizations, reversal number", true),
          new IFB_NUMERIC(/*91*/ 3, "Country code, transaction Dest. Inst.", true),
          new IFB_NUMERIC(/*92*/ 3, "Country code, transaction Orig. Inst.", true),
          new IFB_LLHNUM(/*93*/ 11, "Transaction Dest. Inst. Id code", false),
          new IFB_LLHNUM(/*94*/ 11, "Transaction Orig. Inst. Id code", false),
          new IFB_LLHCHAR(/*95*/ 99, "Card issuer reference data"),
          new IFB_LLLHBINARY(/*96*/ 999, "Key management data"),
          new IFB_AMOUNT(/*97*/ 1 + 16, "Amount, Net reconciliation", true),
          new IF_CHAR(/*98*/ 25, "Payee"),
          new IFB_LLHCHAR(/*99*/ 11, "Settlement institution Id code"),
          new IFB_LLHNUM(/*100*/ 11, "Receiving institution Id code", false),
          new IFB_LLHCHAR(/*101*/ 17, "File name"),
          new IFB_LLHNUM(/*102*/ 28, "Account identification 1", false),
          new IFB_LLHNUM(/*103*/ 28, "Account identification 2", false),
          new IFB_LLLHCHAR(/*104*/ 100, "Transaction description"),
          new IFB_NUMERIC(/*105*/ 16, "Credits, Chargeback amount", true),
          new IFB_NUMERIC(/*106*/ 16, "Debits, Chargeback amount", true),
          new IFB_NUMERIC(/*107*/ 10, "Credits, Chargeback number", true),
          new IFB_NUMERIC(/*108*/ 10, "Debits, Chargeback number", true),
          new IFB_LLHCHAR(/*109*/ 84, "Credits, Fee amounts"),
          new IFB_LLHCHAR(/*110*/ 84, "Debits, Fee amounts"),
          new IFB_LLLHCHAR(/*111*/ 999, "Reserved for ISO use"),
          new IFB_LLLHCHAR(/*112*/ 999, "Reserved for ISO use"),
          new IFB_LLLHCHAR(/*113*/ 999, "Reserved for ISO use"),
          new IFB_LLLHCHAR(/*114*/ 999, "Reserved for ISO use"),
          new IFB_LLLHCHAR(/*115*/ 999, "Reserved for ISO use"),
          new IFB_LLLHCHAR(/*116*/ 999, "Reserved for national use"),
          new IFB_LLLHCHAR(/*117*/ 999, "Reserved for national use"),
          new IFB_LLLHCHAR(/*118*/ 999, "Reserved for national use"),
          new IFB_LLLHCHAR(/*119*/ 999, "Reserved for national use"),
          new IFB_LLLHCHAR(/*120*/ 999, "Reserved for national use"),
          new IFB_LLLHCHAR(/*121*/ 999, "Reserved for national use"),
          new IFB_LLLHCHAR(/*122*/ 999, "Reserved for national use"),
          new IFB_LLLHCHAR(/*123*/ 999, "Reserved for private use"),
          new IFB_LLLHCHAR(/*124*/ 999, "Reserved for private use"),
          new IFB_LLLHCHAR(/*125*/ 999, "Reserved for private use"),
          new IFB_LLLHCHAR(/*126*/ 999, "Reserved for private use"),
          new IFB_LLLHCHAR(/*127*/ 999, "Reserved for private use"),
          new IFB_BINARY(/*128*/ 8, "Message authentication code field"),
  };

  public ISO8583Packer() {
    super();
    super.headerLength = 21;
    setFieldPackager(fields);
  }

  @Override
  public byte[] pack(ISOComponent m) throws ISOException {
    if (((ISOMsg) m).getHeader() == null) {
      ((ISOMsg) m).setHeader(ISO8583Helper.buildHeader());
    }
    byte[] isoMessageBytes = super.pack(m);
    try {
      ByteArrayOutputStream message = new ByteArrayOutputStream();
      message.write(Byte.intTo2Bytes(isoMessageBytes.length));
      message.write(isoMessageBytes);
      return message.toByteArray();
    } catch (IOException e) {
      return isoMessageBytes;
    }
  }

  @Override
  public int unpack(ISOComponent m, byte[] b) throws ISOException {
    int position = Byte.findSubByteArrayPosition(b, "ISO8583".getBytes(StandardCharsets.US_ASCII));
    return super.unpack(m, position != -1 ? Arrays.copyOfRange(b, position, b.length) : b);
  }
}
