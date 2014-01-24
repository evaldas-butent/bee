package com.butent.bee.client.render;

import com.butent.bee.client.images.Images;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

public class ImageRenderer extends AbstractCellRenderer {


  public ImageRenderer(CellSource cellSource) {
    super(cellSource);
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }

    String image = getString(row);
    if (!BeeUtils.isEmpty(image)) {
      if (BeeUtils.isEmpty(Images.getHtml(image))) {
        return null;
      }

      return Images.getHtml(image);
    } else {
      return null;
    }
  }
}
