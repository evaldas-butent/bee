package com.butent.bee.client.modules.finance.analysis;

import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.cell.AbstractCell;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.logging.LogUtils;

import java.util.List;

public class AnalysisResultsGrid extends AbstractGridInterceptor {

  public AnalysisResultsGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new AnalysisResultsGrid();
  }

  @Override
  public boolean afterCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      EditableColumn editableColumn) {

    if ("Open".equals(columnName)) {
      column.getCell().addClickHandler(event -> {
        IsRow row = (event.getSource() instanceof AbstractCell)
            ? ((AbstractCell<?>) event.getSource()).getEventRow() : null;

        if (DataUtils.hasId(row)) {
          open(row.getId());
        }
      });
    }

    return super.afterCreateColumn(columnName, dataColumns, column, header, footer, editableColumn);
  }

  private void open(long id) {
    LogUtils.getRootLogger().debug("open", id);
  }
}
