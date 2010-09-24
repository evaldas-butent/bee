package com.butent.bee.egg.shared.utils;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.Pair;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Codec {
  private static final String SERIALIZATION_SEPARATOR = ";";
  private static final char[] HEX_CHARS = new char[]{
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
      'e', 'f'};

  private static final MessageDigest MD5;
  private static int mdChunk = 0;

  private static final char[] base64Chars = new char[] {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
      'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
      'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
      'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
      '4', '5', '6', '7', '8', '9', '$', '_'};

  private static final byte[] base64Values = new byte[128];

  static {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException ex) {
      md = null;
    }
    MD5 = md;
  }

  static {
    for (int i = 0; i < base64Chars.length; i++) {
      base64Values[base64Chars[i]] = (byte) i;
    }
  }
  
  public static String decodeBase64(String s) {
    return fromBytes(fromBase64(s));
  }

  public static Pair<Integer, Integer> deserializeLength(String src, int start) {
    Assert.notNull(src);
    Assert.nonNegative(start);

    int sLen = src.length();
    Assert.isTrue(start < sLen);

    int z = src.charAt(start) - BeeConst.CHAR_ZERO;
    Assert.nonNegative(z);

    int x;
    if (z == 0) {
      x = 0;
    } else {
      Assert.isTrue(start + z < sLen);
      x = Integer.parseInt(src.substring(start + 1, start + z + 1));
    }

    return new Pair<Integer, Integer>(x, z + 1);
  }

  public static String[] deserializeValues(String ser) {
    Assert.notEmpty(ser);

    int p1 = ser.indexOf(SERIALIZATION_SEPARATOR);
    Assert.isPositive(p1);

    int n = BeeUtils.toInt(ser.substring(0, p1));
    Assert.isPositive(n);

    String[] arr = new String[n];
    int p2, len;

    for (int i = 0; i < n; i++) {
      p2 = ser.indexOf(SERIALIZATION_SEPARATOR, p1 + 1);

      if (p2 == p1 + 1) {
        arr[i] = null;
        p1 = p2;
        continue;
      }

      len = BeeUtils.toInt(ser.substring(p1 + 1, p2));
      if (len > 0) {
        arr[i] = ser.substring(p2 + 1, p2 + 1 + len);
        p1 = p2 + len;
      } else {
        arr[i] = BeeConst.STRING_EMPTY;
        p1 = p2;
      }
    }

    return arr;
  }

  public static String encodeBase64(String s) {
    return toBase64(toBytes(s));
  }

  public static byte[] fromBase64(String data) {
    Assert.notNull(data);
    int len = data.length();
    Assert.isPositive(len);
    Assert.isTrue((len % 4) == 0);

    char[] chars = new char[len];
    data.getChars(0, len, chars, 0);

    int olen = 3 * (len / 4);
    if (chars[len - 2] == '=') {
      --olen;
    }
    if (chars[len - 1] == '=') {
      --olen;
    }

    byte[] bytes = new byte[olen];

    int iidx = 0;
    int oidx = 0;
    while (iidx < len) {
      int c0 = base64Values[chars[iidx++] & 0xff];
      int c1 = base64Values[chars[iidx++] & 0xff];
      int c2 = base64Values[chars[iidx++] & 0xff];
      int c3 = base64Values[chars[iidx++] & 0xff];
      int c24 = (c0 << 18) | (c1 << 12) | (c2 << 6) | c3;

      bytes[oidx++] = (byte) (c24 >> 16);
      if (oidx == olen) {
        break;
      }
      bytes[oidx++] = (byte) (c24 >> 8);
      if (oidx == olen) {
        break;
      }
      bytes[oidx++] = (byte) c24;
    }

    return bytes;
  }

  public static String fromBytes(byte[] bytes) {
    Assert.notNull(bytes);
    int len = bytes.length;
    Assert.isPositive(len);
    Assert.isTrue(len % 2 == 0);

    char[] chars = new char[len / 2];
    for (int i = 0; i < chars.length; i++) {
      chars[i] = (char) ((bytes[i * 2] << 8) + (bytes[i * 2 + 1] & 0xff));
    }

    return new String(chars);
  }
  
  public static String md5(String s) {
    Assert.notNull(MD5);
    Assert.notEmpty(s);

    MD5.reset();

    if (mdChunk > 0) {
      int len = s.length();
      for (int i = 0; i <= (len - 1) / mdChunk; i++) {
        MD5.update(toBytes(s, i * mdChunk, Math.min((i + 1) * mdChunk, len)));
      }
    } else {
      MD5.update(toBytes(s));
    }

    byte[] arr = MD5.digest();

    return toHex(arr);
  }

  public static String serialize(Object obj) {
    if (obj == null) {
      return SERIALIZATION_SEPARATOR;
    } else {
      String s = BeeUtils.transform(obj);
      return s.length() + SERIALIZATION_SEPARATOR + s;
    }
  }

  public static String serializeLength(int len) {
    Assert.nonNegative(len);
    if (len == 0) {
      return BeeConst.STRING_ZERO;
    } else {
      String z = Integer.toString(len);

      StringBuilder sb = new StringBuilder();
      sb.append((char) (BeeConst.CHAR_ZERO + z.length()));
      sb.append(z);

      return sb.toString();
    }
  }
  public static String serializeValues(Object... obj) {
    int n = obj.length;
    Assert.parameterCount(n, 1);

    StringBuilder sb = new StringBuilder();
    sb.append(n);
    sb.append(SERIALIZATION_SEPARATOR);

    for (int i = 0; i < n; i++) {
      sb.append(serialize(obj[i]));
    }

    return sb.toString();
  }

  public static String toBase64(byte[] data) {
    Assert.notNull(data);
    int len = data.length;
    Assert.isPositive(len);

    int olen = 4 * ((len + 2) / 3);
    char[] chars = new char[olen];

    int iidx = 0;
    int oidx = 0;
    int charsLeft = len;
    while (charsLeft > 0) {
      int b0 = data[iidx++] & 0xff;
      int b1 = (charsLeft > 1) ? data[iidx++] & 0xff : 0;
      int b2 = (charsLeft > 2) ? data[iidx++] & 0xff : 0;
      int b24 = (b0 << 16) | (b1 << 8) | b2;

      int c0 = (b24 >> 18) & 0x3f;
      int c1 = (b24 >> 12) & 0x3f;
      int c2 = (b24 >> 6) & 0x3f;
      int c3 = b24 & 0x3f;

      chars[oidx++] = base64Chars[c0];
      chars[oidx++] = base64Chars[c1];
      chars[oidx++] = (charsLeft > 1) ? base64Chars[c2] : '=';
      chars[oidx++] = (charsLeft > 2) ? base64Chars[c3] : '=';

      charsLeft -= 3;
    }

    return new String(chars);
  }

  public static byte[] toBytes(String s) {
    return toBytes(s, 0, BeeUtils.length(s));
  }

  public static byte[] toBytes(String s, int start) {
    return toBytes(s, start, BeeUtils.length(s));
  }

  public static byte[] toBytes(String s, int start, int end) {
    Assert.notNull(s);
    int len = s.length();
    Assert.isPositive(len);

    Assert.nonNegative(start);
    Assert.isTrue(start < end);
    Assert.isTrue(start < len);
    Assert.isTrue(end <= len);

    byte[] arr = new byte[(end - start) * 2];
    char c;

    for (int i = start; i < end; i++) {
      c = s.charAt(i);
      arr[i * 2] = (byte) (c >> 8);
      arr[i * 2 + 1] = (byte) (c & 0xff);
    }

    return arr;
  }
  
  public static String toHex(byte[] bytes) {
    char[] arr = new char[bytes.length * 2];
    int j = 0;

    for (int i = 0; i < bytes.length; i++) {
      arr[j++] = HEX_CHARS[(bytes[i] & 0xF0) >> 4];
      arr[j++] = HEX_CHARS[bytes[i] & 0x0F];
    }

    return new String(arr);
  }

  public static String toHex(char c) {
    return BeeUtils.padLeft(Integer.toHexString(c), 4, BeeConst.CHAR_ZERO);
  }
  
  public static String toHex(char[] arr) {
    if (BeeUtils.isEmpty(arr)) {
      return null;
    } else if (arr.length == 1) {
      return toHex(arr[0]);
    } else {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < arr.length; i++) {
        sb.append(toHex(arr[i]));
      }
      return sb.toString();
    }
  }
  
}
