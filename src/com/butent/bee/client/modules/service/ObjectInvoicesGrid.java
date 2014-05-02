package com.butent.bee.client.modules.service;

import com.google.common.collect.ImmutableMap;

import static com.butent.bee.shared.modules.service.ServiceConstants.COL_MAINTENANCE_INVOICE;
import static com.butent.bee.shared.modules.service.ServiceConstants.COL_SERVICE_OBJECT;
import static com.butent.bee.shared.modules.service.ServiceConstants.VIEW_INVOICES;
import static com.butent.bee.shared.modules.service.ServiceConstants.VIEW_MAINTENANCE;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.ui.Action;

import java.util.Map;

public class ObjectInvoicesGrid extends AbstractGridInterceptor {

  private static final String FILTER_KEY = "f1";
  
  private final String idColumnName;
  
  private Long pendingId;

  ObjectInvoicesGrid() {
    this.idColumnName = Data.getIdColumn(VIEW_INVOICES);
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    if (DataUtils.isId(getPendingId())) {
      presenter.getDataProvider().setParentFilter(FILTER_KEY, getFilter(getPendingId()));
      presenter.handleAction(Action.REFRESH);
      
      setPendingId(null);
    }
  }
  
  @Override
  public Map<String, Filter> getInitialParentFilters() {
    return ImmutableMap.of(FILTER_KEY, Filter.isFalse());
  }
  
  @Override
  public GridInterceptor getInstance() {
    return new ObjectInvoicesGrid();
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    if (getGridPresenter() == null) {
      setPendingId(event.getRowId());
    } else {
      getGridPresenter().getDataProvider().setParentFilter(FILTER_KEY, getFilter(event.getRowId()));
      getGridPresenter().handleAction(Action.REFRESH);
    }
  }

  private Filter getFilter(Long parentId) {
    Filter filter;

    if (DataUtils.isId(parentId)) {
      filter = Filter.in(idColumnName, VIEW_MAINTENANCE, COL_MAINTENANCE_INVOICE,
          Filter.equals(COL_SERVICE_OBJECT, parentId));
    } else {
      filter = Filter.isFalse();
    }

    return filter;
  }

  private Long getPendingId() {
    return pendingId;
  }

  private void setPendingId(Long pendingId) {
    this.pendingId = pendingId;
  }
}
