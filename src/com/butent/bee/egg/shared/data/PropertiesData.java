package com.butent.bee.egg.shared.data;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.Property;

import java.util.List;

public class PropertiesData extends AbstractData {
  private List<Property> data;

  public PropertiesData(List<Property> data) {
    this.data = data;

    setRowCount(data.size());
    setColumnCount(Property.HEADER_COUNT);
  }

  @Override
  public String[] getColumnNames() {
    return Property.HEADERS;
  }

  @Override
  public String getValue(int row, int col) {
    Property el = data.get(row);

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
    Property el = data.get(row);

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
