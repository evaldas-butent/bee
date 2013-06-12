package com.butent.bee.client.render;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;

import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.io.StoredFile;
import com.butent.bee.shared.utils.BeeUtils;

public class FileIconRenderer extends AbstractCellRenderer {
  
  private static final ImageElement imageElement = Document.get().createImageElement(); 

  public FileIconRenderer(CellSource cellSource) {
    super(cellSource);
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }
    
    String icon = getString(row);
    if (BeeUtils.isEmpty(icon)) {
      return null;
    }
    
    String src = StoredFile.getIconUrl(icon);
    if (BeeUtils.isEmpty(src)) {
      return null;
    }
    
    imageElement.setSrc(src);
    imageElement.setAlt(icon);

    return imageElement.getString();
  }
}
