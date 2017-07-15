package com.butent.bee.client.modules.trade;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.modules.trade.TradeKeeper.FilterCallback;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.view.search.AbstractFilterSupplier;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

class DebtsGrid extends AbstractGridInterceptor {

  private static final Dictionary dictionary = Localized.dictionary();

  @Override
  public void afterCreatePresenter(final GridPresenter presenter) {
    HeaderView header = presenter.getHeader();

    header.clearCommandPanel();
    header.addCommandItem(TradeKeeper.createAmountAction(presenter.getViewName(),
        new FilterCallback() {

          @Override
          public Filter getFilter() {
            return presenter.getDataProvider().getFilter();
          }
        }, Data.getIdColumn(presenter.getViewName()),
        presenter.getGridView()));
  }

  @Override
  public AbstractFilterSupplier getFilterSupplier(String columnName,
      ColumnDescription columnDescription) {

    if (BeeUtils.same(columnName, TradeConstants.COL_SALE_LASTEST_PAYMENT)) {
      BeeColumn filterColumn = Data.getColumn(TradeConstants.VIEW_DEBTS, columnName);

      return new NoPaymentPeriodFilterSuppler(getViewName(), filterColumn,
          dictionary.trdNoPaymentPeriod(), columnDescription.getFilterOptions());
    } else {
      return super.getFilterSupplier(columnName, columnDescription);
    }
  }

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

    if (TradeConstants.PROP_AVERAGE_OVERDUE.equals(event.getColumnId())) {
      GridOptions options = GridOptions.forFilter(Filter.equals(
          TradeConstants.COL_TRADE_CUSTOMER, activeRow.getLong(idxCustomer)));

      GridFactory.openGrid(TradeConstants.GRID_ERP_SALES,
          GridFactory.getGridInterceptor(TradeConstants.GRID_ERP_SALES),
          options, PresenterCallback.SHOW_IN_NEW_TAB);
    }

    if (CalendarConstants.COL_APPOINTMENTS_COUNT.equals(event.getColumnId())) {
      GridOptions options = GridOptions.forFilter(Filter.equals(
          ClassifierConstants.COL_COMPANY, activeRow.getLong(idxCustomer)));

      GridFactory.openGrid(CalendarConstants.GRID_APPOINTMENTS,
          GridFactory.getGridInterceptor(CalendarConstants.GRID_APPOINTMENTS),
          options, PresenterCallback.SHOW_IN_NEW_TAB);
    }
  }

}
