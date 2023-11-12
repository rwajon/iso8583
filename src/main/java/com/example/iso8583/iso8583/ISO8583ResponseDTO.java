package com.example.iso8583.iso8583;

import org.jpos.iso.ISOMsg;

public record ISO8583ResponseDTO(ISOMsg isoRequest, ISOMsg isoResponse) {}
