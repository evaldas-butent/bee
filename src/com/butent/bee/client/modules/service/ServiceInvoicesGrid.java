package com.butent.bee.client.modules.service;

import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.modules.trade.TradeKeeper;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.grid.GridView.SelectedRows;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.ui.Action;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServiceInvoicesGrid extends AbstractGridInterceptor implements ClickHandler {

  private final Button action = new Button(Localized.dictionary().trSendToERP(), this);
  private Filter defaultFilter;
  private Filter customFilter = Filter.isTrue();

  public ServiceInvoicesGrid() {
    defaultFilter =
        Filter.isEqual(TradeConstants.COL_TRADE_KIND, Value.getValue(Integer.valueOf(1)));
  }

  @Override
  public void afterCreatePresenter(final GridPresenter presenter) {
    HeaderView header = presenter.getHeader();
    CheckBox showAll = new CheckBox(Localized.dictionary().svcActionShowFromProjects());
    header.clearCommandPanel();
    header.addCommandItem(action);
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
  public Map<String, Filter> getInitialParentFilters(Collection<UiOption> uiOptions) {
    Map<String, Filter> defaultFilters = super.getInitialParentFilters(uiOptions);

    if (defaultFilter != null) {
      if (defaultFilters == null) {
        defaultFilters = Maps.newHashMap();
      }

      defaultFilters.put("CustomFilter", defaultFilter);
    }

    return defaultFilters;
  }

  @Override
  public void onClick(ClickEvent event) {
    final GridPresenter presenter = getGridPresenter();
    final Set<Long> ids = new HashSet<>();

    for (RowInfo row : presenter.getGridView().getSelectedRows(SelectedRows.ALL)) {
      ids.add(row.getId());
    }
    if (ids.isEmpty()) {
      presenter.getGridView().notifyWarning(Localized.dictionary().selectAtLeastOneRow());
      return;
    }

    Global.confirm(Localized.dictionary().trSendToERPConfirm(), new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        final HeaderView header = presenter.getHeader();
        header.clearCommandPanel();
        header.addCommandItem(new Image(Global.getImages().loading()));

        ParameterList args = TradeKeeper.createArgs(TradeConstants.SVC_SEND_TO_ERP);
        args.addDataItem(TradeConstants.VAR_VIEW_NAME, getGridPresenter().getViewName());
        args.addDataItem(TradeConstants.VAR_ID_LIST, DataUtils.buildIdList(ids));

        BeeKeeper.getRpc().makePostRequest(args, getERPResponseCallback());
      }
    });
  }

  private ResponseCallback getERPResponseCallback() {
    final GridPresenter presenter = getGridPresenter();
    final HeaderView header = presenter.getHeader();

    return new ResponseCallback() {

      @Override
      public void onResponse(ResponseObject response) {
        header.clearCommandPanel();
        header.addCommandItem(action);
        response.notify(BeeKeeper.getScreen());
        Data.onViewChange(presenter.getViewName(), DataChangeEvent.CANCEL_RESET_REFRESH);
      }

    };
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
