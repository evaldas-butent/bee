package com.butent.bee.client.modules.service;

import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.HandlesDeleteEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.service.ServiceConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MaintenanceGrid extends AbstractGridInterceptor implements HandlesDeleteEvents {

  private final List<HandlerRegistration> registry = new ArrayList<>();

  MaintenanceGrid() {
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    Button button = new Button(Localized.getConstants().createInvoice());
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        InvoiceBuilder.start(getGridView());
      }
    });

    presenter.getHeader().addCommandItem(button);
  }

  @Override
  public GridInterceptor getInstance() {
    return new MaintenanceGrid();
  }

  @Override
  public void onLoad(GridView gridView) {
    EventUtils.clearRegistry(registry);
    registry.addAll(BeeKeeper.getBus().registerDeleteHandler(this, false));
  }

  @Override
  public void onMultiDelete(MultiDeleteEvent event) {
    if (event != null && isRelevantInvoice(event.getViewName(), event.getRowIds())) {
      getGridPresenter().refresh(false);
    }
  }

  @Override
  public void onRowDelete(RowDeleteEvent event) {
    if (event != null
        && isRelevantInvoice(event.getViewName(), Sets.newHashSet(event.getRowId()))) {
      getGridPresenter().refresh(false);
    }
  }

  @Override
  public void onUnload(GridView gridView) {
    EventUtils.clearRegistry(registry);
  }

  private boolean isRelevantInvoice(String viewName, Collection<Long> ids) {
    if (!BeeUtils.same(viewName, ServiceConstants.VIEW_INVOICES) || BeeUtils.isEmpty(ids)) {
      return false;
    }

    List<IsRow> data = getGridView().getGrid().getRowData();
    if (BeeUtils.isEmpty(data)) {
      return false;
    }

    int index = getDataIndex(ServiceConstants.COL_MAINTENANCE_INVOICE);

    for (IsRow row : data) {
      Long invId = row.getLong(index);
      if (DataUtils.isId(invId) && ids.contains(invId)) {
        return true;
      }
    }
    return false;
  }
}
