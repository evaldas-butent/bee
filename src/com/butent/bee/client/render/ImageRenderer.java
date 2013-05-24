package com.butent.bee.client.render;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;

import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

public class ImageRenderer extends AbstractCellRenderer {

  private static final ImageElement imageElement = Document.get().createImageElement();

  private final int imageIndex;
  private final int nameIndex;
  private final int height;
  private final int width;
  private final String cssClass;

  public ImageRenderer(int imageIndex, int nameIndex, int height, int width, String cssClass) {
    super(null);
    this.imageIndex = imageIndex;
    this.nameIndex = nameIndex;
    this.height = height;
    this.width = width;
    this.cssClass = cssClass;
  }

  public ImageRenderer(int imageIndex, int nameIndex) {
    this(imageIndex, nameIndex, -1, -1, "");
  }

  public ImageRenderer(int imageIndex, int nameIndex, String cssClass) {
    this(imageIndex, nameIndex, -1, -1, cssClass);
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }

    Long fileId = row.getLong(imageIndex);
    String name = row.getString(nameIndex);

    if (!DataUtils.isId(fileId) || BeeUtils.isEmpty(name)) {
      return null;
    }

    imageElement.setAlt(name);
    imageElement.setSrc(FileUtils.getUrl(name, fileId));

    if (height > 0) {
      imageElement.setHeight(height);
    }

    if (width > 0) {
      imageElement.setWidth(width);
    }

    if (!BeeUtils.isEmpty(cssClass)) {
      imageElement.setClassName(cssClass);
    }

    return imageElement.getString();
  }

}
