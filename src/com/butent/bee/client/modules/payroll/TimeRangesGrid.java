package com.butent.bee.client.modules.payroll;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.DataUtils;
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

    if (hasUsage(activeRow)) {
      return DeleteMode.DENY;
    } else {
      return DeleteMode.SINGLE;
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new TimeRangesGrid();
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (event != null && DataUtils.hasId(event.getRowValue()) && hasUsage(event.getRowValue())
        && event.hasAnySource(COL_TR_CODE, COL_TR_FROM, COL_TR_UNTIL, COL_TR_DURATION)
        && !BeeKeeper.getUser().isAdministrator()) {

      event.consume();

    } else {
      super.onEditStart(event);
    }
  }

  private boolean hasUsage(IsRow row) {
    if (DataUtils.hasId(row)) {
      int index = getDataIndex(ALS_TR_USAGE);
      if (index >= 0) {
        return BeeUtils.isPositive(row.getInteger(index));
      }
    }

    return false;
  }
}
