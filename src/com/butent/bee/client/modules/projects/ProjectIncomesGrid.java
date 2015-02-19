package com.butent.bee.client.modules.projects;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.grid.column.CalculatedColumn;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsColumn;

import java.util.List;

public class ProjectIncomesGrid extends AbstractGridInterceptor {

  @Override
  public boolean afterCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      EditableColumn editableColumn) {

    if (column instanceof CalculatedColumn) {
      switch (columnName) {
        case PROP_ITEM_PRICES:

          // int idxItemPrice = DataUtils.getColumnIndex(COL_PROJECT_ITEM_PRICE, dataColumns);
          break;

        default:
          break;
      }

    }

    return super.afterCreateColumn(columnName, dataColumns, column, header, footer, editableColumn);
  }

  @Override
  public GridInterceptor getInstance() {
    return new ProjectIncomesGrid();
  }

}
