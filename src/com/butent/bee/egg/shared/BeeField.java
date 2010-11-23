package com.butent.bee.egg.shared;

import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class BeeField implements HasLongValue, HasStringValue, Transformable {
  private String caption = null;
  private int type = BeeType.TYPE_UNKNOWN;
  private String value = null;

  private BeeWidget widget = null;
  private List<String> items = null;

  private String width = null;

  public BeeField() {
    super();
  }

  public BeeField(int type) {
    this(null, type, null);
  }

  public BeeField(int type, String value) {
    this(null, type, value);
  }

  public BeeField(String caption, int type) {
    this(caption, type, null);
  }

  public BeeField(String caption, int type, String value) {
    this();
    this.caption = caption;
    this.type = type;
    this.value = value;
  }

  public BeeField(String caption, int type, String value, BeeWidget widget) {
    this(caption, type, value);
    this.widget = widget;
  }

  public BeeField(String caption, int type, String value, BeeWidget widget, String... items) {
    this(caption, type, value, widget);

    this.items = new ArrayList<String>();
    for (int i = 0; i < items.length; i++) {
      this.items.add(items[i]);
    }
  }

  public String getCaption() {
    return caption;
  }

  public int getInt() {
    return BeeUtils.toInt(getValue());
  }

  public List<String> getItems() {
    return items;
  }

  public long getLong() {
    return BeeUtils.toLong(getValue());
  }

  public String getString() {
    return getValue();
  }

  public int getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  public BeeWidget getWidget() {
    return widget;
  }

  public String getWidth() {
    return width;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public void setItems(List<String> items) {
    this.items = items;
  }

  public void setType(int type) {
    this.type = type;
  }

  public void setValue(int value) {
    setValue(BeeUtils.toString(value));
  }

  public void setValue(long value) {
    setValue(BeeUtils.toString(value));
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setWidget(BeeWidget widget) {
    this.widget = widget;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  @Override
  public String toString() {
    return BeeUtils.transformOptions("caption", caption, "type", type, "value", value);
  }

  public String transform() {
    return toString();
  }
}
