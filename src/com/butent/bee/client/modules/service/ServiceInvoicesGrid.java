package com.butent.bee.client.modules.service;

import com.google.common.collect.Maps;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.ui.Action;

import java.util.Map;

public class ServiceInvoicesGrid extends AbstractGridInterceptor {

  private static final LocalizableConstants localizableConstants = Localized.getConstants();
  private Filter defaultFilter;
  private Filter customFilter = Filter.isTrue();

  public ServiceInvoicesGrid() {
    defaultFilter =
        Filter.isEqual(TradeConstants.COL_TRADE_KIND, Value.getValue(Integer.valueOf(1)));
  }

  @Override
  public void afterCreatePresenter(final GridPresenter presenter) {
    HeaderView header = presenter.getHeader();
    CheckBox showAll = new CheckBox(localizableConstants.svcActionShowFromProjects());

    header.clearCommandPanel();
    header.addCommandItem(showAll);

    showAll.setChecked(false);
    showAll.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        Assert.notNull(event.getValue());

        if (event.getValue().booleanValue()) {
          showAllRecords(presenter);
        } else {
          setDefaultFilter(presenter);
        }
      }
    });

    setDefaultFilter(presenter);
  }

  @Override
  public void beforeRefresh(GridPresenter presenter) {
    presenter.getDataProvider().setParentFilter("CustomFilter", getFilter());
  }

  @Override
  public GridInterceptor getInstance() {
    return new ServiceInvoicesGrid();
  }

  @Override
  public Map<String, Filter> getInitialParentFilters() {
    Map<String, Filter> defaultFilters = super.getInitialParentFilters();

    if (defaultFilter != null) {
      if (defaultFilters == null) {
        defaultFilters = Maps.newHashMap();
      }

      defaultFilters.put("CustomFilter", defaultFilter);
    }

    return defaultFilters;
  }

  private Filter getFilter() {
    return customFilter;
  }

  private void setDefaultFilter(GridPresenter presenter) {
    setFilter(defaultFilter);
    presenter.handleAction(Action.REFRESH);
  }

  private void setFilter(Filter filter) {
    customFilter = filter;
  }

  private void showAllRecords(GridPresenter presenter) {
    Filter filter =
        Filter.or(defaultFilter, Filter.isEqual(TradeConstants.COL_TRADE_KIND, Value
            .getValue(Integer.valueOf(2))));
    setFilter(filter);
    presenter.handleAction(Action.REFRESH);
  }

}
