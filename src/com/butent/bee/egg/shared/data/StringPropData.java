package com.butent.bee.egg.shared.data;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.StringProp;

import java.util.List;

public class StringPropData extends AbstractData {
  private List<StringProp> data;

  public StringPropData(List<StringProp> data) {
    this.data = data;

    setRowCount(data.size());
    setColumnCount(StringProp.HEADER_COUNT);
  }

  @Override
  public String[] getColumnNames() {
    return StringProp.HEADERS;
  }

  @Override
  public String getValue(int row, int col) {
    StringProp el = data.get(row);

    switch (col) {
      case 0:
        return el.getName();
      case 1:
        return el.getValue();
      default:
        return BeeConst.ERROR;
    }
  }

  @Override
  public void setValue(int row, int col, String value) {
    StringProp el = data.get(row);

    switch (col) {
      case 0:
        el.setName(value);
        break;
      case 1:
        el.setValue(value);
        break;
    }
  }
  
}
