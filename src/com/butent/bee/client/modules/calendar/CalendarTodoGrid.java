package com.butent.bee.client.modules.calendar;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.grid.CellContext;
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
import com.butent.bee.shared.ui.ColumnDescription.ColType;

import java.util.List;

class CalendarTodoGrid extends AbstractGridInterceptor implements ClickHandler {

  CalendarTodoGrid() {
  }

  @Override
  public boolean afterCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      EditableColumn editableColumn) {

    if (column.getColType() == ColType.CALCULATED) {
      column.getCell().addClickHandler(this);
    }

    return super.afterCreateColumn(columnName, dataColumns, column, header, footer, editableColumn);
  }

  @Override
  public GridInterceptor getInstance() {
    return new CalendarTodoGrid();
  }

  @Override
  public void onClick(ClickEvent event) {
    if (event.getSource() instanceof AbstractCell<?>) {
      CellContext context = ((AbstractCell<?>) event.getSource()).getEventContext();
      IsRow row = (context == null) ? null : context.getRow();

      if (DataUtils.hasId(row)) {
        LogUtils.getRootLogger().debug(row.getId(), row.getString(0));
      }
    }
  }
}
