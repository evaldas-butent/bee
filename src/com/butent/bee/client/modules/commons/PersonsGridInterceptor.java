package com.butent.bee.client.modules.commons;

import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.ImageRenderer;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import static com.butent.bee.shared.modules.commons.CommonsConstants.*;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class PersonsGridInterceptor extends AbstractGridInterceptor {

  private static final String CSS_CLASS_STYLE_NAME = "bee-Grid-PersonPhoto";
    
  @Override
  public AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription) {
   
    if (BeeUtils.same(columnName, COL_PHOTO)) {
      return new ImageRenderer(DataUtils.getColumnIndex(columnName, dataColumns),
          DataUtils.getColumnIndex(COL_FIRST_NAME, dataColumns), CSS_CLASS_STYLE_NAME);
    }
    
    return super.getRenderer(columnName, dataColumns, columnDescription);
  }

}
