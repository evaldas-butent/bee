package com.butent.bee.shared.export;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;

public class XSheet implements BeeSerializable {

  public static XSheet restore(String s) {
    Assert.notEmpty(s);
    XSheet sheet = new XSheet();
    sheet.deserialize(s);
    return sheet;
  }

  private String name;

  private final List<XRow> rows = new ArrayList<>();

  private final List<XStyle> styles = new ArrayList<>();

  public XSheet() {
    super();
  }

  public XSheet(String name) {
    this();
    this.name = name;
  }

  public void add(XRow row) {
    Assert.notNull(row);
    rows.add(row);
  }

  public void addHeaders(List<String> headers) {
    addHeaders(headers, XStyle.boldAndCenter(), 0, getMaxColumn() + 1);
  }

  public void addHeaders(List<String> headers, XStyle style, int column, int colSpan) {
    if (BeeUtils.isEmpty(headers)) {
      return;
    }

    for (XRow row : rows) {
      row.shift(headers.size() + 2);
    }

    Integer styleRef = (style == null) ? null : addStyle(style);
    int rowIndex = 1;

    for (String header : headers) {
      if (!BeeUtils.isEmpty(header)) {
        XRow row = new XRow(rowIndex);
        XCell cell = new XCell(column, header);

        if (styleRef != null) {
          cell.setStyleRef(styleRef);
        }
        if (colSpan > 1) {
          cell.setColSpan(colSpan);
        }

        row.add(cell);
        add(row);
      }
      rowIndex++;
    }
  }

  public int addStyle(XStyle style) {
    Assert.notNull(style);

    int index = styles.indexOf(style);
    if (index >= 0) {
      return index;
    } else {
      styles.add(style);
      return styles.size() - 1;
    }
  }

  public void clear() {
    rows.clear();
    styles.clear();
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 3);

    setName(arr[0]);

    if (!rows.isEmpty()) {
      rows.clear();
    }

    String[] rarr = Codec.beeDeserializeCollection(arr[1]);
    if (rarr != null) {
      for (String rv : rarr) {
        add(XRow.restore(rv));
      }
    }

    if (!styles.isEmpty()) {
      styles.clear();
    }

    String[] sarr = Codec.beeDeserializeCollection(arr[2]);
    if (sarr != null) {
      for (String sv : sarr) {
        styles.add(XStyle.restore(sv));
      }
    }
  }

  public int getMaxColumn() {
    int result = BeeConst.UNDEF;
    for (XRow row : rows) {
      result = Math.max(result, row.getMaxColumn());
    }
    return result;
  }

  public String getName() {
    return name;
  }

  public List<XRow> getRows() {
    return rows;
  }

  public XStyle getStyle(int index) {
    return styles.get(index);
  }

  public List<XStyle> getStyles() {
    return styles;
  }

  public boolean isEmpty() {
    return rows.isEmpty() && styles.isEmpty();
  }

  @Override
  public String serialize() {
    List<String> values = new ArrayList<>();

    values.add(getName());
    values.add(rows.isEmpty() ? null : Codec.beeSerialize(rows));
    values.add(styles.isEmpty() ? null : Codec.beeSerialize(styles));

    return Codec.beeSerialize(values);
  }

  public void setName(String name) {
    this.name = name;
  }
}
