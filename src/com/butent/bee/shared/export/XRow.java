package com.butent.bee.shared.export;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;

public class XRow implements BeeSerializable {

  private enum Serial {
    INDEX, STYLE, HEIGHT, CELLS
  }

  public static XRow restore(String s) {
    Assert.notEmpty(s);
    XRow row = new XRow();
    row.deserialize(s);
    return row;
  }

  private int index;

  private XStyle style;

  private int height;

  private final List<XCell> cells = new ArrayList<>();

  public XRow(int index) {
    this.index = index;
  }

  public XRow(int index, XStyle style) {
    this(index);
    this.style = style;
  }

  private XRow() {
    super();
  }

  public void add(XCell cell) {
    Assert.notNull(cell);
    cells.add(cell);
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
        case CELLS:
          String[] carr = Codec.beeDeserializeCollection(value);
          if (carr != null) {
            for (String cv : carr) {
              add(XCell.restore(cv));
            }
          }
          break;
        case HEIGHT:
          setHeight(BeeUtils.toInt(value));
          break;
        case INDEX:
          setIndex(BeeUtils.toInt(value));
          break;
        case STYLE:
          setStyle(XStyle.restore(value));
          break;
      }
    }
  }

  public List<XCell> getCells() {
    return cells;
  }

  public int getHeight() {
    return height;
  }

  public int getIndex() {
    return index;
  }

  public XStyle getStyle() {
    return style;
  }

  public boolean isEmpty() {
    return cells.isEmpty();
  }
  
  @Override
  public String serialize() {
    List<String> values = new ArrayList<>();

    for (Serial member : Serial.values()) {
      switch (member) {
        case CELLS:
          values.add(cells.isEmpty() ? null : Codec.beeSerialize(cells));
          break;
        case HEIGHT:
          values.add(BeeUtils.toString(getHeight()));
          break;
        case INDEX:
          values.add(BeeUtils.toString(getIndex()));
          break;
        case STYLE:
          values.add((getStyle() == null) ? null : getStyle().serialize());
          break;
      }
    }

    return Codec.beeSerialize(values);
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public void setStyle(XStyle style) {
    this.style = style;
  }
}
