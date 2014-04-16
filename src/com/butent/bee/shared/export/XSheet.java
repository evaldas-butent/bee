package com.butent.bee.shared.export;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
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

  private List<XRow> rows = new ArrayList<>();

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
  
  public void clear() {
    rows.clear();
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 2);

    setName(arr[0]);
    String[] rarr = Codec.beeDeserializeCollection(arr[1]);

    if (rarr != null) {
      for (String rv : rarr) {
        add(XRow.restore(rv));
      }
    }
  }

  public String getName() {
    return name;
  }

  public List<XRow> getRows() {
    return rows;
  }
  
  public boolean isEmpty() {
    for (XRow row : rows) {
      if (!row.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String serialize() {
    List<String> values = new ArrayList<>();

    values.add(getName());
    values.add(rows.isEmpty() ? null : Codec.beeSerialize(rows));

    return Codec.beeSerialize(values);
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setRows(List<XRow> rows) {
    this.rows = rows;
  }
}
