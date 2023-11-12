package com.example.iso8583.iso8583;

public final class ISO8583MessageType {
  public static final String AUTHORIZATION_REQUEST = "1100";
  public static final String AUTHORIZATION_RESPONSE = "1110";
  public static final String AUTHORIZATION_ADVICE = "1120";
  public static final String AUTHORIZATION_ADVICE_RESPONSE = "1130";
  public static final String FINANCIAL_TRANSACTION_REQUEST = "1200";
  public static final String FINANCIAL_RESPONSE = "1210";
  public static final String FINANCIAL_TRANSACTION_ADVICE = "1220";
  public static final String FINANCIAL_TRANSACTION_ADVICE_REPEAT = "1221";
  public static final String FINANCIAL_TRANSACTION_ADVICE_RESPONSE = "1230";
  public static final String REVERSAL_ADVICE_RESPONSE = "1430";
  public static final String NETWORK_MANAGEMENT_REQUEST = "1804";
  public static final String NETWORK_MANAGEMENT_RESPONSE = "1814";
}
