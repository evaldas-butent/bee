package com.butent.bee.client.modules.payroll;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.ui.GridDescription;

class ObjectEarningsGrid extends AbstractGridInterceptor {

  private static void requery(GridPresenter presenter, YearMonth ym) {
    if (presenter != null && ym != null) {
      Filter filter = Filter.and(Filter.equals(COL_EARNINGS_YEAR, ym.getYear()),
          Filter.equals(COL_EARNINGS_MONTH, ym.getMonth()));

      presenter.getDataProvider().setDefaultParentFilter(filter);
      presenter.refresh(false, true);
    }
  }

  private YearMonth activeMonth;

  ObjectEarningsGrid() {
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    if (activeMonth != null) {
      requery(presenter, activeMonth);
    }
  }

  @Override
  public BeeRowSet getInitialRowSet(GridDescription gridDescription) {
    return Data.createRowSet(gridDescription.getViewName());
  }

  @Override
  public GridInterceptor getInstance() {
    return new ObjectEarningsGrid();
  }

  void selectMonth(final YearMonth ym) {
    ParameterList params = PayrollKeeper.createArgs(SVC_INIT_EARNINGS);

    params.addQueryItem(COL_EARNINGS_YEAR, ym.getYear());
    params.addQueryItem(COL_EARNINGS_MONTH, ym.getMonth());

    params.setSummary(ym.toString());

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        setActiveMonth(ym);
        if (getGridPresenter() != null) {
          requery(getGridPresenter(), ym);
        }
      }
    });
  }

  private void setActiveMonth(YearMonth activeMonth) {
    this.activeMonth = activeMonth;
  }
}
