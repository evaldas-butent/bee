package com.butent.bee.client.modules.trade;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.CustomAction;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TradeDocumentsGrid extends AbstractGridInterceptor {

  private CustomAction erpAction = new CustomAction(FontAwesome.CLOUD_UPLOAD, event -> toErp());

  private Supplier<Filter> filterSupplier;

  public TradeDocumentsGrid() {
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    if (!BeeUtils.isEmpty(Global.getParameterText(AdministrationConstants.PRM_ERP_ADDRESS))) {
      erpAction.setTitle(Localized.dictionary().trSendToERP());
      presenter.getHeader().addCommandItem(erpAction);
    }
    maybeRefresh(presenter, filterSupplier);
    super.afterCreatePresenter(presenter);
  }

  @Override
  public Set<Action> getDisabledActions(Set<Action> defaultActions) {
    if (Objects.nonNull(filterSupplier)) {
      Set<Action> actionSet = new HashSet<>();
      actionSet.add(Action.ADD);

      if (!BeeUtils.isEmpty(defaultActions)) {
        actionSet.addAll(defaultActions);
      }
      return actionSet;
    }
    return super.getDisabledActions(defaultActions);
  }

  @Override
  public Map<String, Filter> getInitialParentFilters(Collection<UiOption> uiOptions) {
    if (Objects.nonNull(filterSupplier)) {
      return Provider.createDefaultParentFilters(filterSupplier.get());
    }
    return super.getInitialParentFilters(uiOptions);
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeDocumentsGrid();
  }

  @Override
  public boolean isRowEditable(IsRow row) {
    return super.isRowEditable(row) && TradeUtils.isDocumentEditable(row);
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    maybeRefresh(getGridPresenter(), filterSupplier);
    super.onParentRow(event);
  }

  public TradeDocumentsGrid setFilterSupplier(Supplier<Filter> filterSupplier) {
    this.filterSupplier = filterSupplier;
    return this;
  }

  private static void maybeRefresh(GridPresenter presenter, Supplier<Filter> supplier) {
    if (BeeUtils.allNotNull(presenter, supplier)) {
      presenter.getDataProvider().setDefaultParentFilter(supplier.get());
      presenter.handleAction(Action.REFRESH);
    }
  }

  private void toErp() {
    GridView view = getGridView();
    Set<Long> ids = view.getSelectedRows(GridView.SelectedRows.ALL).stream().map(RowInfo::getId)
        .collect(Collectors.toSet());

    if (ids.isEmpty()) {
      view.notifyWarning(Localized.dictionary().selectAtLeastOneRow());
      return;
    }
    Global.confirm(Localized.dictionary().trSendToERPConfirm(), () -> {
      erpAction.running();
      ParameterList args = TradeKeeper.createArgs(TradeConstants.SVC_SEND_TO_ERP);
      args.addDataItem(TradeConstants.VAR_VIEW_NAME, view.getViewName());
      args.addDataItem(TradeConstants.VAR_ID_LIST, DataUtils.buildIdList(ids));

      BeeKeeper.getRpc().makePostRequest(args, response -> {
        erpAction.idle();
        response.notify(view);

        if (response.hasResponse(Integer.class)) {
          Integer cnt = response.getResponseAsInt();

          if (cnt > 0) {
            Data.resetLocal(view.getViewName());
          }
          view.notifyInfo("Eksportuota dokumentų: " + cnt);
        }
      });
    });
  }
}
