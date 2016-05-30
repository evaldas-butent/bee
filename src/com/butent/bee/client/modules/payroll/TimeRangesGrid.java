package com.butent.bee.client.modules.payroll;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

class TimeRangesGrid extends AbstractGridInterceptor {

  TimeRangesGrid() {
  }

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {

    if (activeRow != null) {
      int index = getDataIndex(ALS_TR_USAGE);
      if (index >= 0 && BeeUtils.isPositive(activeRow.getInteger(index))) {
        return DeleteMode.DENY;
      }
    }

    return DeleteMode.SINGLE;
  }

  @Override
  public GridInterceptor getInstance() {
    return new TimeRangesGrid();
  }
}
