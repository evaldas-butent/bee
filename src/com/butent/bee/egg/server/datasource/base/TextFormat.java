package com.butent.bee.egg.server.datasource.base;

import com.ibm.icu.text.UFormat;

import java.text.FieldPosition;
import java.text.ParsePosition;

@SuppressWarnings("serial")
public class TextFormat extends UFormat {

  @Override
  public StringBuffer format(Object obj, StringBuffer appendTo, FieldPosition pos) {
    if ((null == obj) || !(obj instanceof String)) {
      throw new IllegalArgumentException();
    }
    String text = (String) obj;
    appendTo.append(text);
    pos.setBeginIndex(0);
    if (0 == text.length()) {
      pos.setEndIndex(0);
    } else {
      pos.setEndIndex(text.length() - 1);
    }
    return appendTo;
  }

  @Override
  public Object parseObject(String source, ParsePosition pos) {
    if ((null == pos) || (null == source)) {
      throw new NullPointerException();
    }
    pos.setIndex(source.length());
    return source;
  }
}
