package com.butent.bee.shared.communication;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

/**
 * Manages response message object with it's date, level and message parameters and methods for
 * getting and setting these parameters as well as serialization methods.
 */

public class ResponseMessage implements BeeSerializable {
  private DateTime date;
  private LogLevel level;
  private String message;

  public ResponseMessage(DateTime date, LogLevel level, String message) {
    this.date = date;
    this.level = level;
    this.message = message;
  }

  public ResponseMessage(boolean now, LogLevel level, String message) {
    this(now ? new DateTime() : null, level, message);
  }

  public ResponseMessage(boolean now, String message) {
    this(now, null, message);
  }

  public ResponseMessage(LogLevel level, String message) {
    this(null, level, message);
  }

  public ResponseMessage(String message) {
    this(null, null, message);
  }

  public ResponseMessage(String source, boolean serial) {
    if (serial) {
      deserialize(source);
    } else {
      this.message = source;
    }
  }

  @Override
  public void deserialize(String s) {
    Assert.notEmpty(s);

    String src = Codec.decodeBase64(s);

    Pair<Integer, Integer> scan;
    int len = 0;
    int start = 0;

    for (int i = 0; i < 3; i++) {
      scan = Codec.deserializeLength(src, start);
      len = scan.getA();
      start += scan.getB();

      if (len <= 0) {
        continue;
      }
      String v = src.substring(start, start + len);

      switch (i) {
        case 0:
          setDate(new DateTime(Long.parseLong(v)));
          break;
        case 1:
          setLevel(EnumUtils.getEnumByName(LogLevel.class, v));
          break;
        case 2:
          setMessage(v);
          break;
        default:
          Assert.untouchable();
      }

      start += len;
    }
  }

  public DateTime getDate() {
    return date;
  }

  public LogLevel getLevel() {
    return level;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String serialize() {
    StringBuilder sb = new StringBuilder();

    Codec.serializeWithLength(sb, date);
    Codec.serializeWithLength(sb, level);
    Codec.serializeWithLength(sb, message);

    return Codec.encodeBase64(sb.toString());
  }

  public void setDate(DateTime date) {
    this.date = date;
  }

  public void setLevel(LogLevel level) {
    this.level = level;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return BeeUtils.joinWords(date, level, message);
  }
}
