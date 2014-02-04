package com.butent.bee.client.render;

import com.butent.bee.client.images.Images;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.html.builder.elements.Sup;
import com.butent.bee.shared.utils.BeeUtils;

public class AttachmentRenderer extends AbstractCellRenderer {

  public static final String IMAGE_ATTACHMENT = "attachment";

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
      String val = Images.getHtml(IMAGE_ATTACHMENT);
      Sup sup = new Sup().text(BeeUtils.toString(count));
      return val + sup.build();
    } else {
      return null;
    }
  }
}
