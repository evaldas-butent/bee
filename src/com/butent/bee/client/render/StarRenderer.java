package com.butent.bee.client.render;

import com.google.gwt.resources.client.ImageResource;

import com.butent.bee.client.images.star.Stars;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.export.XCell;
import com.butent.bee.shared.export.XPicture;
import com.butent.bee.shared.export.XSheet;

public class StarRenderer extends AbstractCellRenderer {

  public StarRenderer(CellSource cellSource) {
    super(cellSource);
  }

  @Override
  public XCell export(IsRow row, int cellIndex, Integer styleRef, XSheet sheet) {
    Integer index = getInteger(row);
    if (index == null || sheet == null) {
      return null;
    }

    ImageResource resource = Stars.get(index);
    if (resource == null) {
      return null;
    }

    XPicture picture = XPicture.create(resource.getSafeUri().asString());
    if (picture == null) {
      return null;
    }

    int ref = sheet.registerPicture(picture);
    return XCell.forPicture(cellIndex, ref);
  }

  @Override
  public String render(IsRow row) {
    Integer index = getInteger(row);

    if (index == null) {
      return null;
    } else {
      return Stars.getHtml(index);
    }
  }
}
