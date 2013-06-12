package com.butent.bee.client.render;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;

import com.butent.bee.client.images.Flags;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

public class FlagRenderer extends AbstractCellRenderer {
  
  private static final ImageElement imageElement = Document.get().createImageElement(); 

  public FlagRenderer(CellSource cellSource) {
    super(cellSource);
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }
    
    String key = getString(row);
    if (BeeUtils.isEmpty(key)) {
      return null;
    }
    
    String uri = Flags.get(key);
    if (BeeUtils.isEmpty(uri)) {
      return null;
    }
    
    imageElement.setSrc(uri);
    imageElement.setAlt(key);
    
    return imageElement.getString();
  }
}
