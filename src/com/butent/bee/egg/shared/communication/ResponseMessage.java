package com.butent.bee.egg.shared.communication;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeDate;
import com.butent.bee.egg.shared.BeeSerializable;
import com.butent.bee.egg.shared.Pair;
import com.butent.bee.egg.shared.Transformable;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.Codec;
import com.butent.bee.egg.shared.utils.LogUtils;

import java.util.logging.Level;

public class ResponseMessage implements BeeSerializable, Transformable {
  private BeeDate date = null;
  private Level level = null;
  private String message = null;

  public ResponseMessage(BeeDate date, Level level, String message) {
    this.date = date;
    this.level = level;
    this.message = message;
  }

  public ResponseMessage(boolean now, Level level, String message) {
    this(now ? new BeeDate() : null, level, message);
  }

  public ResponseMessage(boolean now, String message) {
    this(now, null, message);
  }

  public ResponseMessage(Level level, String message) {
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
  
  public void deserialize(String s) {
    Assert.notEmpty(s);
    
    String src = Codec.decodeBase64(s);

    Pair<Integer, Integer> scan;
    int len, start = 0;
    
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
          setDate(new BeeDate(v));
          break;
        case 1:
          setLevel(Level.parse(v));
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

  public BeeDate getDate() {
    return date;
  }

  public Level getLevel() {
    return level;
  }

  public String getMessage() {
    return message;
  }

  public String serialize() {
    StringBuilder sb = new StringBuilder();
    
    Codec.serializeWithLength(sb, date);
    Codec.serializeWithLength(sb, LogUtils.transformLevel(level));
    Codec.serializeWithLength(sb, message);
    
    return Codec.encodeBase64(sb.toString());
  }

  public void setDate(BeeDate date) {
    this.date = date;
  }

  public void setLevel(Level level) {
    this.level = level;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return BeeUtils.concat(1, date, level, message);
  }

  public String transform() {
    return toString();
  }

}
