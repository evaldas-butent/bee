package com.butent.bee.egg.server.datasource.base;

import com.butent.bee.egg.shared.Assert;
import com.ibm.icu.text.UFormat;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;

@SuppressWarnings("serial")
public class BooleanFormat extends UFormat {

  private String trueString;
  private String falseString;

  public BooleanFormat() {
    this("true", "false");
  }

  public BooleanFormat(String pattern) {
    String[] valuePatterns = pattern.split(":");
    if (valuePatterns.length != 2) {
      throw new IllegalArgumentException("Cannot construct a boolean format "
          + "from " + pattern + ". The pattern must contain a single ':' "
          + "character");
    }
    this.trueString = valuePatterns[0];
    this.falseString = valuePatterns[1];
  }

  public BooleanFormat(String trueString, String falseString) {
    Assert.notNull(trueString);
    Assert.notNull(falseString);

    this.trueString = trueString;
    this.falseString = falseString;
  }

  @Override
  public StringBuffer format(Object obj, StringBuffer appendTo, FieldPosition pos) {
    if ((null != obj) && !(obj instanceof Boolean)) {
      throw new IllegalArgumentException();
    }
    Boolean val = (Boolean) obj;
    if (val == null) {
      pos.setBeginIndex(0);
      pos.setEndIndex(0);
    } else if (val) {
      appendTo.append(trueString);
      pos.setBeginIndex(0);
      pos.setEndIndex(trueString.length() - 1);
    } else {
      appendTo.append(falseString);
      pos.setBeginIndex(0);
      pos.setEndIndex(falseString.length() - 1);
    }
    return appendTo;
  }

  public Boolean parse(String text) throws ParseException {
    ParsePosition parsePosition = new ParsePosition(0);
    Boolean result = parseObject(text, parsePosition);
    if (parsePosition.getIndex() == 0) {
      throw new ParseException("Unparseable boolean: \"" + text + '"',
          parsePosition.getErrorIndex());
    }
    return result;
  }

  @Override
  public Boolean parseObject(String source, ParsePosition pos) {
    if (source == null) {
      throw new NullPointerException();
    }
    Boolean value = null;
    if (trueString.equalsIgnoreCase(source.trim())) {
      value = Boolean.TRUE;
      pos.setIndex(trueString.length());
    } else if (falseString.equalsIgnoreCase(source.trim())) {
      value = Boolean.FALSE;
      pos.setIndex(falseString.length());
    }
    if (null == value) {
      pos.setErrorIndex(0);
    }
    return value;
  }
}
