package com.butent.bee.client.modules.ec;

import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class EcOrderItemsHandler extends AbstractGridInterceptor {

  private static final class PropertiesRenderer extends AbstractCellRenderer {

    private PropertiesRenderer() {
      super(null);
    }

    @Override
    public String render(IsRow row) {
      if (row == null) {
        return null;
      }
      
      if (BeeUtils.isEmpty(row.getProperties())) {
        return null;
      } else {
        return row.getProperties().toString();
      }
    }
  }

  EcOrderItemsHandler() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new EcOrderItemsHandler();
  }

  @Override
  public AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription) {

    if (BeeUtils.same(columnName, "Properties")) {
      return new PropertiesRenderer();
    } else {
      return super.getRenderer(columnName, dataColumns, columnDescription);
    }
  }
}
