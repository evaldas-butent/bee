package com.butent.bee.shared;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements operations with variables in the system.
 */

public class Variable implements HasBooleanValue, HasDoubleValue, HasStringValue {

  private final BeeType type;
  private String caption = null;
  private String value = null;

  private BeeWidget widget = null;
  private List<String> items = null;

  private String width = null;

  public Variable(BeeType type) {
    this(null, type, null);
  }

  public Variable(BeeType type, String value) {
    this(null, type, value);
  }

  public Variable(String caption, BeeType type) {
    this(caption, type, null);
  }

  public Variable(String caption, BeeType type, String value) {
    this.caption = caption;
    this.type = type;
    this.value = value;
  }

  public Variable(String caption, BeeType type, String value, BeeWidget widget) {
    this(caption, type, value);
    this.widget = widget;
  }

  public Variable(String caption, BeeType type, String value, BeeWidget widget, String... items) {
    this(caption, type, value, widget);

    if (items != null) {
      this.items = new ArrayList<String>();
      for (int i = 0; i < items.length; i++) {
        this.items.add(items[i]);
      }
    }
  }

  public boolean getBoolean() {
    return BeeUtils.toBoolean(getValue());
  }

  public String getCaption() {
    return caption;
  }

  public double getDouble() {
    return BeeUtils.toDouble(getValue());
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

  public BeeType getType() {
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

  public void setValue(Boolean value) {
    setValue(BeeUtils.toString(value));
  }

  public void setValue(double value) {
    setValue(BeeUtils.toString(value));
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
    return BeeUtils.joinOptions("caption", caption, "type", BeeUtils.toString(type),
        "value", value);
  }
}
