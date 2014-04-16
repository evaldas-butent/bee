package com.butent.bee.shared.export;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;

public class XWorkbook implements BeeSerializable {

  private String name;

  private List<XSheet> sheets = new ArrayList<>();

  public XWorkbook() {
    super();
  }

  public XWorkbook(String name) {
    this();
    this.name = name;
  }

  public void add(XSheet sheet) {
    Assert.notNull(sheet);
    sheets.add(sheet);
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 2);

    setName(arr[0]);

    String[] sarr = Codec.beeDeserializeCollection(arr[1]);
    if (sarr != null) {
      for (String v : sarr) {
        add(XSheet.restore(v));
      }
    }
  }

  public String getName() {
    return name;
  }

  public List<XSheet> getSheets() {
    return sheets;
  }

  public boolean isEmpty() {
    for (XSheet sheet : sheets) {
      if (!sheet.isEmpty()) {
        return false;
      }
    }
    return true;
  }
  
  @Override
  public String serialize() {
    List<String> values = new ArrayList<>();

    values.add(getName());
    values.add(sheets.isEmpty() ? null : Codec.beeSerialize(sheets));

    return Codec.beeSerialize(values);
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSheets(List<XSheet> sheets) {
    this.sheets = sheets;
  }
}
