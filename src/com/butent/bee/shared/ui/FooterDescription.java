package com.butent.bee.shared.ui;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;
import java.util.Map;

public class FooterDescription implements BeeSerializable, HasInfo, HasOptions {

  private enum Serial {
    SUM, TEXT, HTML, FORMAT, HOR_ALIGN, SCALE, OPTIONS
  }

  private static final String ATTR_SUM = "sum";
  
  public static FooterDescription restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    FooterDescription footerDescription = new FooterDescription();
    footerDescription.deserialize(s);
    return footerDescription;
  }

  private String sum;

  private String text;
  private String html;

  private String format;
  private String horAlign;

  private Integer scale;

  private String options;

  public FooterDescription(Map<String, String> attributes) {
    setAttributes(attributes);
  }

  private FooterDescription() {
  }
  
  public FooterDescription copy() {
    return restore(serialize());
  }
  
  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      switch (member) {
        case SUM:
          setSum(value);
          break;
        case TEXT:
          setText(value);
          break;
        case HTML:
          setHtml(value);
          break;
        case FORMAT:
          setFormat(value);
          break;
        case HOR_ALIGN:
          setHorAlign(value);
          break;
        case SCALE:
          setScale(BeeUtils.toIntOrNull(value));
          break;
        case OPTIONS:
          setOptions(value);
          break;
      }
    }
  }

  public String getFormat() {
    return format;
  }

  public String getHorAlign() {
    return horAlign;
  }

  public String getHtml() {
    return html;
  }

  @Override
  public List<Property> getInfo() {
    List<Property> info = PropertyUtils.createProperties(
        "Sum", getSum(),
        "Text", getText(),
        "Html", getHtml(),
        "Format", getFormat(),
        "Horizontal Alignment", getHorAlign(),
        "Scale", getScale(),
        "Options", getOptions());
    
    PropertyUtils.addWhenEmpty(info, getClass());
    return info;
  }

  @Override
  public String getOptions() {
    return options;
  }

  public Integer getScale() {
    return scale;
  }

  public String getSum() {
    return sum;
  }

  public String getText() {
    return text;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case SUM:
          arr[i++] = getSum();
          break;
        case TEXT:
          arr[i++] = getText();
          break;
        case HTML:
          arr[i++] = getHtml();
          break;
        case FORMAT:
          arr[i++] = getFormat();
          break;
        case HOR_ALIGN:
          arr[i++] = getHorAlign();
          break;
        case SCALE:
          arr[i++] = getScale();
          break;
        case OPTIONS:
          arr[i++] = getOptions();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setAttributes(Map<String, String> attributes) {
    if (attributes == null || attributes.isEmpty()) {
      return;
    }

    for (Map.Entry<String, String> attribute : attributes.entrySet()) {
      String key = attribute.getKey();
      String value = BeeUtils.trim(attribute.getValue());
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      if (BeeUtils.same(key, ATTR_SUM)) {
        setSum(value);
      } else if (BeeUtils.same(key, UiConstants.ATTR_TEXT)) {
        setText(value);
      } else if (BeeUtils.same(key, UiConstants.ATTR_HTML)) {
        setHtml(value);
      } else if (BeeUtils.same(key, UiConstants.ATTR_FORMAT)) {
        setFormat(value);
      } else if (BeeUtils.same(key, UiConstants.ATTR_HORIZONTAL_ALIGNMENT)) {
        setHorAlign(value);
      } else if (BeeUtils.same(key, UiConstants.ATTR_SCALE)) {
        setScale(BeeUtils.toIntOrNull(value));
      } else if (BeeUtils.same(key, ATTR_OPTIONS)) {
        setOptions(value);
      }
    }
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public void setHorAlign(String horAlign) {
    this.horAlign = horAlign;
  }

  public void setHtml(String html) {
    this.html = html;
  }

  @Override
  public void setOptions(String options) {
    this.options = options;
  }

  public void setScale(Integer scale) {
    this.scale = scale;
  }

  public void setSum(String sum) {
    this.sum = sum;
  }

  public void setText(String text) {
    this.text = text;
  }
}
