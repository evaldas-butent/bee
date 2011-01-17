package com.butent.bee.server.datasource.render;

import com.butent.bee.shared.utils.Codec;

public class EscapeUtil {

  private static final char[] HEX_DIGITS = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  };

  public static String htmlEscape(String str) {
    return Codec.escapeHtml(str);
  }

  public static String jsonEscape(String str) {
    if (str == null || str.length() == 0) {
      return "";
    }
    StringBuffer sb = new StringBuffer();
    char current;
    for (int i = 0, j = str.length(); i < j; ++i) {
      current = str.charAt(i);
      switch (current) {
        case '\'':
          sb.append("\\u0027");
          break;
        case '\"':
          sb.append("\\u0022");
          break;
        case '\\':
          sb.append('\\');
          sb.append(current);
          break;
        case '<':
          sb.append("\\u003c");
          break;
        case '>':
          sb.append("\\u003e");
          break;
        default:
          if (current < ' ' || (current >= '\u0080' && current < '\u00a0') ||
              (current >= '\u2000' && current < '\u2100')) {
            sb.append('\\');
            switch (current) {
              case '\b':
                sb.append('b');
                break;
              case '\t':
                sb.append('t');
                break;
              case '\n':
                sb.append('n');
                break;
              case '\f':
                sb.append('f');
                break;
              case '\r':
                sb.append('r');
                break;
              default:
                sb.append('u');
                sb.append(HEX_DIGITS[(current >> 12) & 0xF]);
                sb.append(HEX_DIGITS[(current >>  8) & 0xF]);
                sb.append(HEX_DIGITS[(current >>  4) & 0xF]);
                sb.append(HEX_DIGITS[current & 0xF]);
            }
          } else {
            sb.append(current);
          }
      }
    }
    return sb.toString();
  }

  private EscapeUtil() {
  }
}
