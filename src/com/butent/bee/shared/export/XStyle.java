package com.butent.bee.shared.export;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.css.values.FontStyle;
import com.butent.bee.shared.css.values.FontWeight;
import com.butent.bee.shared.css.values.TextAlign;
import com.butent.bee.shared.css.values.VerticalAlign;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;

public class XStyle implements BeeSerializable {

  private enum Serial {
    COLOR, BACKGROUND_COLOR, FONT_NAME, FONT_HEIGHT, FONT_WEIGHT, FONT_STYLE,
    FORMAT, TEXT_ALIGN, VERTICAL_ALIGN
  }

  public static XStyle bold() {
    XStyle style = new XStyle();
    style.setFontWeight(FontWeight.BOLD);
    return style;
  }

  public static XStyle boldAndRight() {
    XStyle style = bold();
    style.setTextAlign(TextAlign.RIGHT);
    return style;
  }

  public static XStyle restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    } else {
      XStyle style = new XStyle();
      style.deserialize(s);
      return style;
    }
  }

  public static XStyle right() {
    XStyle style = new XStyle();
    style.setTextAlign(TextAlign.RIGHT);
    return style;
  }

  private String color;
  private String backgroundColor;

  private String fontName;
  private int fontHeight;
  private FontWeight fontWeight;
  private FontStyle fontStyle;

  private String format;

  private TextAlign textAlign;
  private VerticalAlign verticalAlign;

  public XStyle() {
    super();
  }

  public XStyle copy() {
    XStyle result = new XStyle();

    result.setColor(getColor());
    result.setBackgroundColor(getBackgroundColor());

    result.setFontName(getFontName());
    result.setFontHeight(getFontHeight());
    result.setFontWeight(getFontWeight());
    result.setFontStyle(getFontStyle());

    result.setFormat(getFormat());

    result.setTextAlign(getTextAlign());
    result.setVerticalAlign(getVerticalAlign());

    return result;
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      String value = arr[i];
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      switch (members[i]) {
        case BACKGROUND_COLOR:
          setBackgroundColor(value);
          break;
        case COLOR:
          setColor(value);
          break;

        case FONT_HEIGHT:
          setFontHeight(BeeUtils.toInt(value));
          break;
        case FONT_NAME:
          setFontName(value);
          break;
        case FONT_STYLE:
          setFontStyle(Codec.unpack(FontStyle.class, value));
          break;
        case FONT_WEIGHT:
          setFontWeight(Codec.unpack(FontWeight.class, value));
          break;

        case FORMAT:
          setFormat(value);
          break;

        case TEXT_ALIGN:
          setTextAlign(Codec.unpack(TextAlign.class, value));
          break;
        case VERTICAL_ALIGN:
          setVerticalAlign(Codec.unpack(VerticalAlign.class, value));
          break;
      }
    }
  }

  public String getBackgroundColor() {
    return backgroundColor;
  }

  public String getColor() {
    return color;
  }

  public int getFontHeight() {
    return fontHeight;
  }

  public String getFontName() {
    return fontName;
  }

  public FontStyle getFontStyle() {
    return fontStyle;
  }

  public FontWeight getFontWeight() {
    return fontWeight;
  }

  public String getFormat() {
    return format;
  }

  public TextAlign getTextAlign() {
    return textAlign;
  }

  public VerticalAlign getVerticalAlign() {
    return verticalAlign;
  }
  
  public boolean hasFont() {
    return !BeeUtils.isEmpty(getFontName()) || getFontHeight() > 0
        || getFontWeight() != null || getFontStyle() != null;
  }

  public XStyle merge(XStyle other) {
    XStyle result = copy();
    if (other == null) {
      return result;
    }

    if (BeeUtils.isEmpty(getColor()) && !BeeUtils.isEmpty(other.getColor())) {
      result.setColor(other.getColor());
    }
    if (BeeUtils.isEmpty(getBackgroundColor()) && !BeeUtils.isEmpty(other.getBackgroundColor())) {
      result.setBackgroundColor(other.getBackgroundColor());
    }

    if (BeeUtils.isEmpty(getFontName()) && !BeeUtils.isEmpty(other.getFontName())) {
      result.setFontName(other.getFontName());
    }
    if (!BeeUtils.isPositive(getFontHeight()) && BeeUtils.isPositive(other.getFontHeight())) {
      result.setFontHeight(other.getFontHeight());
    }
    if (getFontWeight() == null && other.getFontWeight() != null) {
      result.setFontWeight(other.getFontWeight());
    }
    if (getFontStyle() == null && other.getFontStyle() != null) {
      result.setFontStyle(other.getFontStyle());
    }

    if (BeeUtils.isEmpty(getFormat()) && !BeeUtils.isEmpty(other.getFormat())) {
      result.setFormat(other.getFormat());
    }

    if (getTextAlign() == null && other.getTextAlign() != null) {
      result.setTextAlign(other.getTextAlign());
    }
    if (getVerticalAlign() == null && other.getVerticalAlign() != null) {
      result.setVerticalAlign(other.getVerticalAlign());
    }

    return result;
  }

  @Override
  public String serialize() {
    List<String> values = new ArrayList<>();

    for (Serial member : Serial.values()) {
      switch (member) {
        case BACKGROUND_COLOR:
          values.add(getBackgroundColor());
          break;
        case COLOR:
          values.add(getColor());
          break;

        case FONT_HEIGHT:
          values.add(BeeUtils.toString(getFontHeight()));
          break;
        case FONT_NAME:
          values.add(getFontName());
          break;
        case FONT_STYLE:
          values.add(Codec.pack(getFontStyle()));
          break;
        case FONT_WEIGHT:
          values.add(Codec.pack(getFontWeight()));
          break;

        case FORMAT:
          values.add(getFormat());
          break;

        case TEXT_ALIGN:
          values.add(Codec.pack(getTextAlign()));
          break;
        case VERTICAL_ALIGN:
          values.add(Codec.pack(getVerticalAlign()));
          break;
      }
    }

    return Codec.beeSerialize(values);
  }

  public void setBackgroundColor(String backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public void setFontHeight(int fontHeight) {
    this.fontHeight = fontHeight;
  }

  public void setFontName(String fontName) {
    this.fontName = fontName;
  }

  public void setFontStyle(FontStyle fontStyle) {
    this.fontStyle = fontStyle;
  }

  public void setFontWeight(FontWeight fontWeight) {
    this.fontWeight = fontWeight;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public void setTextAlign(TextAlign textAlign) {
    this.textAlign = textAlign;
  }

  public void setVerticalAlign(VerticalAlign verticalAlign) {
    this.verticalAlign = verticalAlign;
  }
}
