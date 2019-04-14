package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
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
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;
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

  private Filter operationsFilter;

  private TradeDocumentPhase defaultPhase;

  public TradeDocumentsGrid() {
  }

  public TradeDocumentsGrid(long typeId) {
    super();
    Queries.getRow(VIEW_TRADE_DOCUMENT_TYPES, typeId, result ->
        this.setDefaultPhase(Arrays.stream(TradeDocumentPhase.values())
            .filter(phase ->
                Data.isTrue(VIEW_TRADE_DOCUMENT_TYPES, result, phase.getDocumentTypeColumnName()))
            .findFirst()
            .orElse(TradeDocumentPhase.PENDING)));

    Queries.getDistinctLongs(TradeConstants.TBL_TRADE_TYPE_OPERATIONS,
        TradeConstants.COL_TRADE_OPERATION, Filter.equals(TradeConstants.COL_DOCUMENT_TYPE, typeId),
        result -> this.setOperationsFilter(Filter.idIn(result)));
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
    return new TradeDocumentsGrid().setDefaultPhase(this.defaultPhase)
        .setOperationsFilter(getOperationsFilter());
  }

  public Filter getOperationsFilter() {
    return operationsFilter;
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

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow, boolean copy) {
    newRow.setValue(gridView.getDataIndex(COL_TRADE_DOCUMENT_PHASE), this.defaultPhase);
    return super.onStartNewRow(gridView, oldRow, newRow, copy);
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

  private TradeDocumentsGrid setDefaultPhase(TradeDocumentPhase phase) {
    this.defaultPhase = phase;
    return this;
  }

  private TradeDocumentsGrid setOperationsFilter(Filter operationsFilter) {
    this.operationsFilter = operationsFilter;
    return this;
  }

  private void toErp() {
    GridView view = getGridView();
    Set<Long> ids = view.getSelectedRows(GridView.SelectedRows.ALL).stream().map(RowInfo::getId)
        .collect(Collectors.toSet());

    if (ids.isEmpty()) {
      view.notifyWarning(Localized.dictionary().selectAtLeastOneRow());
      return;
    }
    Queries.getRowCount(view.getViewName(), Filter.and(Filter.idIn(ids),
        Filter.notEquals(TradeConstants.COL_TRADE_DOCUMENT_PHASE, TradeDocumentPhase.APPROVED)),
        count -> {
          if (count > 0) {
            view.notifyWarning("Leidžiami eksportuoti tik patvirtinti dokumentai");
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
        });
  }
}
