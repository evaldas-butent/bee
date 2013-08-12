package com.butent.bee.client.modules.ec;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;

import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class ArticleGraphicsHandler extends AbstractGridInterceptor {

  private static final class PictureRenderer extends AbstractCellRenderer {
    
    private static final ImageElement imageElement;
    
    static {
      imageElement = Document.get().createImageElement();
      imageElement.setAlt("picture");
      
      imageElement.setClassName(EcStyles.name("Picture"));
    }

    private PictureRenderer() {
      super(null);
    }

    @Override
    public String render(IsRow row) {
      if (row == null) {
        return null;
      }
      
      String picture = row.getProperty(EcConstants.COL_TCD_GRAPHICS_RESOURCE);
      if (picture == null) {
        return null;
      }
      
      imageElement.setSrc(picture);
      return imageElement.getString();
    }
  }
  
  ArticleGraphicsHandler() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new ArticleGraphicsHandler();
  }
  
  @Override
  public AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription) {

    if (BeeUtils.same(columnName, EcConstants.COL_TCD_GRAPHICS_RESOURCE)) {
      return new PictureRenderer();
    } else {
      return super.getRenderer(columnName, dataColumns, columnDescription);
    }
  }
}
