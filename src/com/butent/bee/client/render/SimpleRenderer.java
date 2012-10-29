package com.butent.bee.client.render;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;

public class SimpleRenderer extends AbstractCellRenderer {

  public SimpleRenderer(int dataIndex, IsColumn dataColumn) {
    super(Assert.nonNegative(dataIndex), dataColumn);
  }

  public SimpleRenderer(int dataIndex, ValueType dataType) {
    super(Assert.nonNegative(dataIndex), dataType);
  }

  public SimpleRenderer(int dataIndex) {
    this(dataIndex, ValueType.TEXT);
  }
  
  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    } else {
      return DataUtils.render(row, getDataIndex(), getDataType());
    }
  }
}
