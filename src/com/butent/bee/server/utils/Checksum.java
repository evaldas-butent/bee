package com.butent.bee.server.utils;

import com.butent.bee.shared.utils.Codec;

import java.util.zip.Adler32;
import java.util.zip.CRC32;

import sun.misc.CRC16;

/**
 * Enables to control whether information package was sent correctly through the internet by using
 * crc16, crc32 and adler32 checksum functions.
 */

public class Checksum {
  public static String adler32(byte[] arr) {
    Adler32 cs = new Adler32();
    cs.update(arr);
    long value = cs.getValue();

    return Integer.toHexString((int) value);
  }

  public static String adler32(String input) {
    return adler32(Codec.toBytes(input));
  }

  public static String crc16(byte[] arr) {
    CRC16 cs = new CRC16();
    for (int i = 0; i < arr.length; i++) {
      cs.update(arr[i]);
    }
    int value = cs.value;

    return Integer.toHexString(value);
  }

  public static String crc16(String input) {
    return crc16(Codec.toBytes(input));
  }

  public static String crc32(byte[] arr) {
    CRC32 cs = new CRC32();
    cs.update(arr);
    long value = cs.getValue();

    return Integer.toHexString((int) value);
  }

  public static String crc32(String input) {
    return crc32(Codec.toBytes(input));
  }

  private Checksum() {
  }
}
