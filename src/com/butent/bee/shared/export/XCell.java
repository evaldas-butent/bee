package com.butent.bee.shared.export;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;

public class XCell implements BeeSerializable {

  private enum Serial {
    INDEX, VALUE, FORMULA, STYLE, COL_SPAN, ROW_SPAN
  }

  public static XCell restore(String s) {
    Assert.notEmpty(s);
    XCell cell = new XCell();
    cell.deserialize(s);
    return cell;
  }

  private int index;

  private Value value;
  private String formula;

  private XStyle style;

  private int colSpan;
  private int rowSpan;

  private XCell() {
    super();
  }

  public XCell(int index, String v) {
    this(index, new TextValue(v));
  }

  public XCell(int index, Double v) {
    this(index, new NumberValue(v));
  }

  public XCell(int index, Double v, XStyle style) {
    this(index, new NumberValue(v), style);
  }

  public XCell(int index, Integer v) {
    this(index, new IntegerValue(v));
  }

  public XCell(int index, Integer v, XStyle style) {
    this(index, new IntegerValue(v), style);
  }

  public XCell(int index, Long v) {
    this(index, new LongValue(v));
  }

  public XCell(int index, Long v, XStyle style) {
    this(index, new LongValue(v), style);
  }

  public XCell(int index, Value value) {
    this.index = index;
    this.value = value;
  }

  public XCell(int index, Value value, XStyle style) {
    this(index, value);
    this.style = style;
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      String v = arr[i];
      if (BeeUtils.isEmpty(v)) {
        continue;
      }

      switch (members[i]) {
        case COL_SPAN:
          setColSpan(BeeUtils.toInt(v));
          break;
        case FORMULA:
          setFormula(v);
          break;
        case INDEX:
          setIndex(BeeUtils.toInt(v));
          break;
        case ROW_SPAN:
          setRowSpan(BeeUtils.toInt(v));
          break;
        case STYLE:
          setStyle(XStyle.restore(v));
          break;
        case VALUE:
          setValue(Value.restore(v));
          break;
      }
    }
  }

  public int getColSpan() {
    return colSpan;
  }

  public String getFormula() {
    return formula;
  }

  public int getIndex() {
    return index;
  }

  public int getRowSpan() {
    return rowSpan;
  }

  public XStyle getStyle() {
    return style;
  }

  public Value getValue() {
    return value;
  }

  @Override
  public String serialize() {
    List<String> values = new ArrayList<>();

    for (Serial member : Serial.values()) {
      switch (member) {
        case COL_SPAN:
          values.add(BeeUtils.toString(getColSpan()));
          break;
        case FORMULA:
          values.add(getFormula());
          break;
        case INDEX:
          values.add(BeeUtils.toString(getIndex()));
          break;
        case ROW_SPAN:
          values.add(BeeUtils.toString(getRowSpan()));
          break;
        case STYLE:
          values.add((getStyle() == null) ? null : getStyle().serialize());
          break;
        case VALUE:
          values.add((getValue() == null) ? null : getValue().serialize());
          break;
      }
    }

    return Codec.beeSerialize(values);
  }

  public void setColSpan(int colSpan) {
    this.colSpan = colSpan;
  }

  public void setFormula(String formula) {
    this.formula = formula;
  }

  public void setRowSpan(int rowSpan) {
    this.rowSpan = rowSpan;
  }

  public void setStyle(XStyle style) {
    this.style = style;
  }

  public void setValue(Value value) {
    this.value = value;
  }

  private void setIndex(int index) {
    this.index = index;
  }
}
