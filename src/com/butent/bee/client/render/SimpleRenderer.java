package com.butent.bee.client.render;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

public class SimpleRenderer extends AbstractCellRenderer {

  public SimpleRenderer(int dataIndex, IsColumn dataColumn) {
    super(Assert.nonNegative(dataIndex), Assert.notNull(dataColumn));
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    } else {
      return DataUtils.getValue(row, getDataIndex(), getDataType());
    }
  }
}
