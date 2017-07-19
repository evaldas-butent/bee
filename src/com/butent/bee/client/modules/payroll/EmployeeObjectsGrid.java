package com.butent.bee.client.modules.payroll;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.RowInfo;

import java.util.Collection;

class EmployeeObjectsGrid extends AbstractGridInterceptor {

  EmployeeObjectsGrid() {
  }

  @Override
  public boolean isRowEditable(IsRow row) {
    return row != null && !DataUtils.isId(row.getLong(getDataIndex(COL_WOKR_SCHEDULE_LOCK)));
  }

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {

    if (DataUtils.isId(activeRow.getLong(getDataIndex(COL_WOKR_SCHEDULE_LOCK)))) {
      return DeleteMode.DENY;
    } else {
      return DeleteMode.SINGLE;
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new EmployeeObjectsGrid();
  }
}
