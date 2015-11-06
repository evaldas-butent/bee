package com.butent.bee.client.modules.payroll;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.GridDescription;

class EmployeeEarningsGrid extends AbstractGridInterceptor {

  EmployeeEarningsGrid() {
  }

  @Override
  public BeeRowSet getInitialRowSet(GridDescription gridDescription) {
    return Data.createRowSet(gridDescription.getViewName());
  }

  @Override
  public GridInterceptor getInstance() {
    return new EmployeeEarningsGrid();
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    Filter filter = null;

    if (event.getRow() != null) {
      Long object = Data.getLong(event.getViewName(), event.getRow(), COL_PAYROLL_OBJECT);
      Integer year = Data.getInteger(event.getViewName(), event.getRow(), COL_EARNINGS_YEAR);
      Integer month = Data.getInteger(event.getViewName(), event.getRow(), COL_EARNINGS_MONTH);

      if (DataUtils.isId(object) && TimeUtils.isYear(year) && TimeUtils.isMonth(month)) {
        filter = Filter.and(Filter.equals(COL_PAYROLL_OBJECT, object),
            Filter.equals(COL_EARNINGS_YEAR, year),
            Filter.equals(COL_EARNINGS_MONTH, month));
      }
    }

    if (filter == null) {
      filter = Filter.isFalse();
    }

    if (getGridPresenter() != null) {
      getGridPresenter().getDataProvider().setDefaultParentFilter(filter);
      getGridPresenter().refresh(false, true);
    }
  }
}
