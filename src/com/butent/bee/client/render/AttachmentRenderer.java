package com.butent.bee.client.render;

import com.butent.bee.client.images.Images;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

public class AttachmentRenderer extends AbstractCellRenderer {

  public static final String IMAGE_ATTACHMENT = "attachmnet";

  public AttachmentRenderer(CellSource cellSource) {
    super(cellSource);
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }

    Integer count = getInteger(row);
    if (BeeUtils.isPositive(count)) {
      return Images.getHtml(IMAGE_ATTACHMENT);
    } else {
      return null;
    }
  }
}
