package com.butent.bee.client.render;

import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.io.Paths;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

public class PhotoRenderer extends AbstractCellRenderer {
  
  public static final String STYLE_EMBEDDED = "bee-Photo-embedded";

  private static final ImageElement imageElement;
  
  private static final Map<String, String> urlCache;

  static {
    imageElement = Document.get().createImageElement();
    imageElement.addClassName(STYLE_EMBEDDED);
    
    urlCache = Maps.newHashMap();
  }
  
  public static void addToCache(String fileName, String url) {
    Assert.notEmpty(fileName);
    Assert.notEmpty(url);
    
    urlCache.put(fileName, url);
  }
  
  public static String getUrl(String fileName) {
    if (BeeUtils.isEmpty(fileName)) {
      return null;
    } else if (urlCache.containsKey(fileName)) {
      return urlCache.get(fileName);
    } else {
      return getPath(fileName);
    }
  }
  
  private static String getPath(String fileName) {
    return Paths.buildPath(Paths.IMAGE_DIR, Paths.PHOTO_DIR, fileName);
  }

  public PhotoRenderer(CellSource cellSource) {
    super(cellSource);
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }
    
    String fileName = getString(row); 
    if (BeeUtils.isEmpty(fileName)) {
      return null;
    }

    imageElement.setSrc(getUrl(fileName));
    imageElement.setAlt("photo");

    return imageElement.getString();
  }
}
