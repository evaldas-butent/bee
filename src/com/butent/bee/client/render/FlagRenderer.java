package com.butent.bee.client.render;

import com.butent.bee.client.images.Flags;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

import elemental.client.Browser;

import elemental.html.ImageElement;

public class FlagRenderer extends AbstractCellRenderer {
  
  private static final ImageElement imageElement = Browser.getDocument().createImageElement(); 

  public FlagRenderer(int dataIndex) {
    super(Assert.nonNegative(dataIndex), ValueType.TEXT);
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
    return imageElement.getOuterHTML();
  }
}
