package com.butent.bee.client.render;

import com.butent.bee.client.images.star.Stars;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;

public class StarRenderer extends AbstractCellRenderer {
  
  public StarRenderer(CellSource cellSource) {
    super(cellSource);
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
