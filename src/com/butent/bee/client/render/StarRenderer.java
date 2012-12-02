package com.butent.bee.client.render;

import com.butent.bee.client.images.star.Stars;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;

public class StarRenderer extends AbstractCellRenderer {
  
  public StarRenderer(int dataIndex) {
    super(Assert.nonNegative(dataIndex), ValueType.INTEGER);
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }
    
    Integer index = getInteger(row);
    if (index == null) {
      return null;
    } else {
      return Stars.getHtml(index);
    }
  }
}
