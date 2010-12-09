package com.butent.bee.egg.shared.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.ExtendedProperty;

import java.util.List;

public class ExtendedPropertiesData extends AbstractData {
  private List<ExtendedProperty> data;

  public ExtendedPropertiesData(List<ExtendedProperty> data) {
    this.data = data;

    setRowCount(data.size());
    setColumnCount(ExtendedProperty.COLUMN_COUNT);
  }

  @Override
  public String[] getColumnNames() {
    return ExtendedProperty.COLUMN_HEADERS;
  }

  @Override
  public String getValue(int row, int col) {
    ExtendedProperty el = data.get(row);

    switch (col) {
      case 0:
        return el.getName();
      case 1:
        return el.getSub();
      case 2:
        return el.getValue();
      case 3:
        return el.getDate().toLog();
      default:
        return BeeConst.ERROR;
    }
  }

  @Override
  public void setValue(int row, int col, String value) {
    ExtendedProperty el = data.get(row);

    switch (col) {
      case 0:
        el.setName(value);
        break;
      case 1:
        el.setSub(value);
        break;
      case 2:
        el.setValue(value);
        break;
      default:
        Assert.untouchable();
    }
  }
  
}
