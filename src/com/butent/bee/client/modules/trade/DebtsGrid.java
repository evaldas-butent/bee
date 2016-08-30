package com.butent.bee.client.modules.trade;

import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.utils.BeeUtils;

class DebtsGrid extends AbstractGridInterceptor {

  @Override
  public GridInterceptor getInstance() {
    return new DebtsGrid();
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    IsRow activeRow = event.getRowValue();

    if (activeRow == null) {
      return;
    }
    int idxCustomer = getDataIndex(TradeConstants.COL_TRADE_CUSTOMER);
    int idxPayer = getDataIndex(TradeConstants.COL_SALE_PAYER);

    if (CalendarConstants.COL_APPOINTMENTS_COUNT.equals(event.getColumnId())) {
      Long id = BeeUtils.nvl(activeRow.getLong(idxPayer), activeRow.getLong(idxCustomer));
      GridOptions options = GridOptions.forFilter(Filter.equals(
          ClassifierConstants.COL_COMPANY, id));

      GridFactory.openGrid(CalendarConstants.GRID_APPOINTMENTS,
          GridFactory.getGridInterceptor(CalendarConstants.GRID_APPOINTMENTS),
          options, PresenterCallback.SHOW_IN_NEW_TAB);
    }
  }

}