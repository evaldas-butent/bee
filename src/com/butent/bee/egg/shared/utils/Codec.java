package com.butent.bee.egg.shared.utils;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeSerializable;
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

  private static final char[] base64Chars = new char[]{
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
      'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
      'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
      'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
      '4', '5', '6', '7', '8', '9', '$', '_'};
  private static final byte[] base64Values = new byte[128];
  private static final char base64Pad = '=';

  private static final int[] crc16Table = {
      0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241, 0xC601,
      0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440, 0xCC01, 0x0CC0,
      0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40, 0x0A00, 0xCAC1, 0xCB81,
      0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841, 0xD801, 0x18C0, 0x1980, 0xD941,
      0x1B00, 0xDBC1, 0xDA81, 0x1A40, 0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01,
      0x1DC0, 0x1C80, 0xDC41, 0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0,
      0x1680, 0xD641, 0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081,
      0x1040, 0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
      0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441, 0x3C00,
      0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41, 0xFA01, 0x3AC0,
      0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840, 0x2800, 0xE8C1, 0xE981,
      0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41, 0xEE01, 0x2EC0, 0x2F80, 0xEF41,
      0x2D00, 0xEDC1, 0xEC81, 0x2C40, 0xE401, 0x24C0, 0x2580, 0xE541, 0x2700,
      0xE7C1, 0xE681, 0x2640, 0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0,
      0x2080, 0xE041, 0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281,
      0x6240, 0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
      0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41, 0xAA01,
      0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840, 0x7800, 0xB8C1,
      0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41, 0xBE01, 0x7EC0, 0x7F80,
      0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40, 0xB401, 0x74C0, 0x7580, 0xB541,
      0x7700, 0xB7C1, 0xB681, 0x7640, 0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101,
      0x71C0, 0x7080, 0xB041, 0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0,
      0x5280, 0x9241, 0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481,
      0x5440, 0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
      0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841, 0x8801,
      0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40, 0x4E00, 0x8EC1,
      0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41, 0x4400, 0x84C1, 0x8581,
      0x4540, 0x8701, 0x47C0, 0x4680, 0x8641, 0x8201, 0x42C0, 0x4380, 0x8341,
      0x4100, 0x81C1, 0x8081, 0x4040};

  private static final int[] crc32Table = {
      0x00000000, 0x77073096, 0xee0e612c, 0x990951ba, 0x076dc419, 0x706af48f,
      0xe963a535, 0x9e6495a3, 0x0edb8832, 0x79dcb8a4, 0xe0d5e91e, 0x97d2d988,
      0x09b64c2b, 0x7eb17cbd, 0xe7b82d07, 0x90bf1d91, 0x1db71064, 0x6ab020f2,
      0xf3b97148, 0x84be41de, 0x1adad47d, 0x6ddde4eb, 0xf4d4b551, 0x83d385c7,
      0x136c9856, 0x646ba8c0, 0xfd62f97a, 0x8a65c9ec, 0x14015c4f, 0x63066cd9,
      0xfa0f3d63, 0x8d080df5, 0x3b6e20c8, 0x4c69105e, 0xd56041e4, 0xa2677172,
      0x3c03e4d1, 0x4b04d447, 0xd20d85fd, 0xa50ab56b, 0x35b5a8fa, 0x42b2986c,
      0xdbbbc9d6, 0xacbcf940, 0x32d86ce3, 0x45df5c75, 0xdcd60dcf, 0xabd13d59,
      0x26d930ac, 0x51de003a, 0xc8d75180, 0xbfd06116, 0x21b4f4b5, 0x56b3c423,
      0xcfba9599, 0xb8bda50f, 0x2802b89e, 0x5f058808, 0xc60cd9b2, 0xb10be924,
      0x2f6f7c87, 0x58684c11, 0xc1611dab, 0xb6662d3d, 0x76dc4190, 0x01db7106,
      0x98d220bc, 0xefd5102a, 0x71b18589, 0x06b6b51f, 0x9fbfe4a5, 0xe8b8d433,
      0x7807c9a2, 0x0f00f934, 0x9609a88e, 0xe10e9818, 0x7f6a0dbb, 0x086d3d2d,
      0x91646c97, 0xe6635c01, 0x6b6b51f4, 0x1c6c6162, 0x856530d8, 0xf262004e,
      0x6c0695ed, 0x1b01a57b, 0x8208f4c1, 0xf50fc457, 0x65b0d9c6, 0x12b7e950,
      0x8bbeb8ea, 0xfcb9887c, 0x62dd1ddf, 0x15da2d49, 0x8cd37cf3, 0xfbd44c65,
      0x4db26158, 0x3ab551ce, 0xa3bc0074, 0xd4bb30e2, 0x4adfa541, 0x3dd895d7,
      0xa4d1c46d, 0xd3d6f4fb, 0x4369e96a, 0x346ed9fc, 0xad678846, 0xda60b8d0,
      0x44042d73, 0x33031de5, 0xaa0a4c5f, 0xdd0d7cc9, 0x5005713c, 0x270241aa,
      0xbe0b1010, 0xc90c2086, 0x5768b525, 0x206f85b3, 0xb966d409, 0xce61e49f,
      0x5edef90e, 0x29d9c998, 0xb0d09822, 0xc7d7a8b4, 0x59b33d17, 0x2eb40d81,
      0xb7bd5c3b, 0xc0ba6cad, 0xedb88320, 0x9abfb3b6, 0x03b6e20c, 0x74b1d29a,
      0xead54739, 0x9dd277af, 0x04db2615, 0x73dc1683, 0xe3630b12, 0x94643b84,
      0x0d6d6a3e, 0x7a6a5aa8, 0xe40ecf0b, 0x9309ff9d, 0x0a00ae27, 0x7d079eb1,
      0xf00f9344, 0x8708a3d2, 0x1e01f268, 0x6906c2fe, 0xf762575d, 0x806567cb,
      0x196c3671, 0x6e6b06e7, 0xfed41b76, 0x89d32be0, 0x10da7a5a, 0x67dd4acc,
      0xf9b9df6f, 0x8ebeeff9, 0x17b7be43, 0x60b08ed5, 0xd6d6a3e8, 0xa1d1937e,
      0x38d8c2c4, 0x4fdff252, 0xd1bb67f1, 0xa6bc5767, 0x3fb506dd, 0x48b2364b,
      0xd80d2bda, 0xaf0a1b4c, 0x36034af6, 0x41047a60, 0xdf60efc3, 0xa867df55,
      0x316e8eef, 0x4669be79, 0xcb61b38c, 0xbc66831a, 0x256fd2a0, 0x5268e236,
      0xcc0c7795, 0xbb0b4703, 0x220216b9, 0x5505262f, 0xc5ba3bbe, 0xb2bd0b28,
      0x2bb45a92, 0x5cb36a04, 0xc2d7ffa7, 0xb5d0cf31, 0x2cd99e8b, 0x5bdeae1d,
      0x9b64c2b0, 0xec63f226, 0x756aa39c, 0x026d930a, 0x9c0906a9, 0xeb0e363f,
      0x72076785, 0x05005713, 0x95bf4a82, 0xe2b87a14, 0x7bb12bae, 0x0cb61b38,
      0x92d28e9b, 0xe5d5be0d, 0x7cdcefb7, 0x0bdbdf21, 0x86d3d2d4, 0xf1d4e242,
      0x68ddb3f8, 0x1fda836e, 0x81be16cd, 0xf6b9265b, 0x6fb077e1, 0x18b74777,
      0x88085ae6, 0xff0f6a70, 0x66063bca, 0x11010b5c, 0x8f659eff, 0xf862ae69,
      0x616bffd3, 0x166ccf45, 0xa00ae278, 0xd70dd2ee, 0x4e048354, 0x3903b3c2,
      0xa7672661, 0xd06016f7, 0x4969474d, 0x3e6e77db, 0xaed16a4a, 0xd9d65adc,
      0x40df0b66, 0x37d83bf0, 0xa9bcae53, 0xdebb9ec5, 0x47b2cf7f, 0x30b5ffe9,
      0xbdbdf21c, 0xcabac28a, 0x53b39330, 0x24b4a3a6, 0xbad03605, 0xcdd70693,
      0x54de5729, 0x23d967bf, 0xb3667a2e, 0xc4614ab8, 0x5d681b02, 0x2a6f2b94,
      0xb40bbe37, 0xc30c8ea1, 0x5a05df1b, 0x2d02ef8d};

  private static final int adler32Base = 65521;
  
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
  
  public static String adler32(String input) {
    return adler32(toBytes(input));
  }

  public static String adler32(byte[] arr) {
    int s1 = 1;
    int s2 = 0;

    for (byte b : arr) {
      s1 = (s1 + (b & 0xff)) % adler32Base;
      s2 = (s2 + s1) % adler32Base;
    }

    int value = (s2 << 16) | s1;
    return Integer.toHexString(value);
  }

  public static String crc16(String input) {
    return crc16(toBytes(input));
  }
  
  public static String crc16(byte[] arr) {
    int crc = 0;

    for (byte b : arr) {
      crc = (crc >>> 8) ^ crc16Table[(crc ^ b) & 0xff];
    }

    return Integer.toHexString(crc);
  }

  public static String crc32(String input) {
    return crc32(toBytes(input));
  }

  public static String crc32(byte[] arr) {
    int crc = 0xffffffff;

    for (byte b : arr) {
      crc = (crc >>> 8) ^ crc32Table[(crc ^ b) & 0xff];
    }

    crc = crc ^ 0xffffffff;
    return Integer.toHexString(crc);
  }

  public static String crc32Direct(String input) {
    return crc32Direct(toBytes(input));
  }

  public static String crc32Direct(byte[] arr) {
    int crc = 0xffffffff;
    int poly = 0xEDB88320;

    for (byte b : arr) {
      int z = (crc ^ b) & 0xff;

      for (int i = 0; i < 8; i++) {
        if ((z & 1) == 1) {
          z = (z >>> 1) ^ poly;
        } else {
          z = (z >>> 1);
        }
      }
      crc = (crc >>> 8) ^ z;
    }

    crc = crc ^ 0xffffffff;
    return Integer.toHexString(crc);
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
    if (chars[len - 2] == base64Pad) {
      --olen;
    }
    if (chars[len - 1] == base64Pad) {
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
    Assert.isEven(len);

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
  
  public static void serializeWithLength(StringBuilder sb, Object obj) {
    Assert.notNull(sb);
    if (obj == null) {
      sb.append(BeeConst.CHAR_ZERO);
      return;
    }
    
    String v;
    if (obj instanceof BeeSerializable) {
      v = ((BeeSerializable) obj).serialize();
    } else {
      v = BeeUtils.transformNoTrim(obj);
    }

    int len = v.length();
    if (len <= 0) {
      sb.append(BeeConst.CHAR_ZERO);
    } else {
      sb.append(serializeLength(len));
      sb.append(v);
    }
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
      chars[oidx++] = (charsLeft > 1) ? base64Chars[c2] : base64Pad;
      chars[oidx++] = (charsLeft > 2) ? base64Chars[c3] : base64Pad;

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
