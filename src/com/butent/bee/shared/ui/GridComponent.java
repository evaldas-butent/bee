package com.butent.bee.shared.ui;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

public class GridComponent implements BeeSerializable, HasInfo {

  private enum SerializationMember {
    STYLE, HEIGHT, MIN_HEIGHT, MAX_HEIGHT, PADDING, BORDER_WIDTH, MARGIN
  }

  public static GridComponent restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    GridComponent component = new GridComponent();
    component.deserialize(s);
    return component;
  }

  private Style style = null;

  private Integer height = null;
  private Integer minHeight = null;
  private Integer maxHeight = null;

  private String padding = null;
  private String borderWidth = null;
  private String margin = null;

  public GridComponent() {
  }

  public void deserialize(String s) {
    SerializationMember[] members = SerializationMember.values();
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      switch (members[i]) {
        case STYLE:
          setStyle(Style.restore(arr[i]));
          break;
        case HEIGHT:
          setHeight(BeeUtils.toIntOrNull(arr[i]));
          break;
        case MIN_HEIGHT:
          setMinHeight(BeeUtils.toIntOrNull(arr[i]));
          break;
        case MAX_HEIGHT:
          setMaxHeight(BeeUtils.toIntOrNull(arr[i]));
          break;
        case PADDING:
          setPadding(arr[i]);
          break;
        case BORDER_WIDTH:
          setBorderWidth(arr[i]);
          break;
        case MARGIN:
          setMargin(arr[i]);
          break;
      }
    }
  }

  public List<Property> getInfo() {
    List<Property> info = Lists.newArrayList();
    if (getStyle() != null) {
      info.addAll(getStyle().getInfo());
    }
    
    PropertyUtils.addProperties(info,
        "Height", getHeight(), "Min Height", getMinHeight(), "Max Height", getMaxHeight(),
        "Padding", getPadding(), "Border Width", getBorderWidth(), "Margin", getMargin());
    
    PropertyUtils.addWhenEmpty(info, getClass());
    return info;
  }

  public boolean isEmpty() {
    return (getStyle() == null || getStyle().isEmpty())
        && BeeUtils.allEmpty(getHeight(), getMinHeight(), getMaxHeight(), getPadding(),
            getBorderWidth(), getMargin());
  }

  public String serialize() {
    SerializationMember[] members = SerializationMember.values();
    Object[] arr = new Object[members.length];

    for (int i = 0; i < members.length; i++) {
      switch (members[i]) {
        case STYLE:
          arr[i] = getStyle();
          break;
        case HEIGHT:
          arr[i] = getHeight();
          break;
        case MIN_HEIGHT:
          arr[i] = getMinHeight();
          break;
        case MAX_HEIGHT:
          arr[i] = getMaxHeight();
          break;
        case PADDING:
          arr[i] = getPadding();
          break;
        case BORDER_WIDTH:
          arr[i] = getBorderWidth();
          break;
        case MARGIN:
          arr[i] = getMargin();
          break;
      }
    }
    return Codec.beeSerializeAll(arr);
  }

  private String getBorderWidth() {
    return borderWidth;
  }

  private Integer getHeight() {
    return height;
  }

  private String getMargin() {
    return margin;
  }

  private Integer getMaxHeight() {
    return maxHeight;
  }

  private Integer getMinHeight() {
    return minHeight;
  }

  private String getPadding() {
    return padding;
  }

  private Style getStyle() {
    return style;
  }

  private void setBorderWidth(String borderWidth) {
    this.borderWidth = borderWidth;
  }

  private void setHeight(Integer height) {
    this.height = height;
  }

  private void setMargin(String margin) {
    this.margin = margin;
  }

  private void setMaxHeight(Integer maxHeight) {
    this.maxHeight = maxHeight;
  }

  private void setMinHeight(Integer minHeight) {
    this.minHeight = minHeight;
  }

  private void setPadding(String padding) {
    this.padding = padding;
  }

  private void setStyle(Style style) {
    this.style = style;
  }
}
