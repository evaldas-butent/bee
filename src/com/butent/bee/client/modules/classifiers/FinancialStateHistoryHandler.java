package com.butent.bee.client.modules.classifiers;

import com.google.common.collect.ImmutableMap;

import com.butent.bee.client.data.LocalProvider;
import com.butent.bee.client.modules.administration.HistoryHandler;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.ui.GridDescription;

import java.util.Collection;
import java.util.Map;

class FinancialStateHistoryHandler extends HistoryHandler {
  private static final String FILTER_KEY = "f1";

  public FinancialStateHistoryHandler(String viewName, Collection<Long> ids) {
    super(viewName, ids);
  }

  private static Filter getFilter() {
    Filter filter =
        Filter.isEqual(AdministrationConstants.COL_RELATION,
            Value.getValue(ClassifierConstants.VIEW_FINANCIAL_STATES));

    filter = Filter.and(filter, Filter.notNull(AdministrationConstants.COL_RELATION));

    return filter;
  }

  @Override
  public Map<String, Filter> getInitialParentFilters() {
    return ImmutableMap.of(FILTER_KEY, getFilter());
  }

  @Override
  public boolean initDescription(GridDescription gridDescription) {
    gridDescription.setFilter(getFilter());
    return true;
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    if (presenter != null && presenter.getDataProvider() instanceof LocalProvider) {
      LocalProvider provider = (LocalProvider) presenter.getDataProvider();
      provider.setUserFilter(getFilter());
      provider.setParentFilter(FILTER_KEY, getFilter());
      
      super.afterCreatePresenter(presenter);
    }
  }
}
