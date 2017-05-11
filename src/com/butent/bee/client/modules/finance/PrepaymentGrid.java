package com.butent.bee.client.modules.finance;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.cell.AbstractCell;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.finance.PrepaymentKind;

import java.util.List;

class PrepaymentGrid extends FinanceGrid {

  final PrepaymentKind kind;

  PrepaymentGrid(PrepaymentKind kind) {
    super();
    this.kind = kind;
  }

  @Override
  public GridInterceptor getInstance() {
    return new PrepaymentGrid(kind);
  }

  @Override
  public boolean afterCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      EditableColumn editableColumn) {

    if (PROP_PREPAYMENT_USED.equals(columnName)) {
      column.getCell().addClickHandler(event -> {
        IsRow row = AbstractCell.getEventRow(event);
        if (isUsed(row)) {
          showUsage(row.getId());
        }
      });
    }

    return super.afterCreateColumn(columnName, dataColumns, column, header, footer, editableColumn);
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (event.hasSource(COL_FIN_CURRENCY) && isUsed(event.getRowValue())) {
      event.consume();
    } else {
      super.onEditStart(event);
    }
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow, boolean copy) {
    newRow.setValue(getDataIndex(COL_FIN_PREPAYMENT_KIND), kind);
    return super.onStartNewRow(gridView, oldRow, newRow, copy);
  }

  private static boolean isUsed(IsRow row) {
    return row != null && row.hasPropertyValue(PROP_PREPAYMENT_USED);
  }

  private static void showUsage(long id) {
    LogUtils.getRootLogger().debug("usage", id);
  }
}
