package com.example.iso8583.util;

import java.nio.ByteBuffer;

public final class Byte {
  private Byte() {}

  public static int findSubByteArrayPosition(byte[] byteArray, byte[] targetArray) {
    for (int i = 0; i <= byteArray.length - targetArray.length; i++) {
      boolean found = true;
      for (int j = 0; j < targetArray.length; j++) {
        if (byteArray[i + j] != targetArray[j]) {
          found = false;
          break;
        }
      }
      if (found) {
        return i; // Return the position if the sub-array is found
      }
    }
    return -1; // Return -1 if the sub-array is not found in the array
  }

  public static byte[] intTo4Bytes(int number) {
    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
    buffer.putInt(number);
    return buffer.array();
  }

  public static byte[] intTo2Bytes(int number) {
    ByteBuffer buffer = ByteBuffer.allocate(2);
    buffer.putShort((short) number);
    return buffer.array();
  }
}
