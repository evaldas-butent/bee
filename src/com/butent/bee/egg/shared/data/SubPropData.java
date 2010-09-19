package com.butent.bee.egg.shared.data;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeDate;
import com.butent.bee.egg.shared.utils.SubProp;

import java.util.List;

public class SubPropData extends AbstractData {
  private List<SubProp> data;

  public SubPropData(List<SubProp> data) {
    this.data = data;

    setRowCount(data.size());
    setColumnCount(SubProp.COLUMN_COUNT);
  }

  @Override
  public String[] getColumnNames() {
    return SubProp.COLUMN_HEADERS;
  }

  @Override
  public String getValue(int row, int col) {
    SubProp el = data.get(row);

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
    SubProp el = data.get(row);

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
      case 4:
        el.setDate(new BeeDate(BeeDate.parse(value)));
        break;
    }
  }
  
}
