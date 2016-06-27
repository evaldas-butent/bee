package com.butent.bee.client.render;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;

import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.Map;

public class PhotoRenderer extends AbstractCellRenderer {

  public static final String STYLE_EMBEDDED = BeeConst.CSS_CLASS_PREFIX + "Photo-embedded";

  public static final String DEFAULT_PHOTO_IMAGE = "images/defaultUser.png";

  private static final ImageElement imageElement;

  private static final Map<String, String> urlCache;

  static {
    imageElement = Document.get().createImageElement();
    imageElement.addClassName(STYLE_EMBEDDED);

    urlCache = new HashMap<>();
  }

  public static void addToCache(String fileName, String url) {
    Assert.notEmpty(fileName);
    Assert.notEmpty(url);

    urlCache.put(fileName, url);
  }

  public static String getUrl(Long fileId) {
    if (!DataUtils.isId(fileId)) {
      return null;
    } else {
      return FileUtils.getUrl(fileId);
    }
  }

  public PhotoRenderer(CellSource cellSource) {
    super(cellSource);
  }

  @Override
  public String render(IsRow row) {
    String src = getSrc(row);
    if (BeeUtils.isEmpty(src)) {
      return null;
    }

    imageElement.setSrc(src);
    imageElement.setAlt("photo");

    return imageElement.getString();
  }

  private String getSrc(IsRow row) {
    Long fileId = getLong(row);

    if (!DataUtils.isId(fileId)) {
      return DEFAULT_PHOTO_IMAGE;
    } else {
      return getUrl(fileId);
    }
  }
}
