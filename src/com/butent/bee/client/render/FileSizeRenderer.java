package com.butent.bee.client.render;

import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

public class FileSizeRenderer extends AbstractCellRenderer {

  public FileSizeRenderer(CellSource cellSource) {
    super(cellSource);
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }

    Long size = getLong(row);
    return BeeUtils.isPositive(size) ? FileUtils.sizeToText(size) : null;
  }
}
