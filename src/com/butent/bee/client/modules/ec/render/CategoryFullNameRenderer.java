package com.butent.bee.client.modules.ec.render;

import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.shared.data.IsRow;

public class CategoryFullNameRenderer extends AbstractCellRenderer {
  
  private final int categoryIndex;
  private final String separator;

  public CategoryFullNameRenderer(int categoryIndex, String separator) {
    super(null);

    this.categoryIndex = categoryIndex;
    this.separator = separator;
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }
    
    Long category = row.getLong(categoryIndex);
    if (category == null) {
      return null;
    } else {
      return EcKeeper.getCategoryFullName(category, separator);
    }
  }
}
