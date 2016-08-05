package com.butent.bee.client.modules.trade;

import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.DecimalLabel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeDiscountMode;
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.butent.bee.shared.modules.trade.TradeDocumentSums;
import com.butent.bee.shared.modules.trade.TradeVatMode;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collections;

public class TradeDocumentForm extends AbstractFormInterceptor {

  private static final String NAME_AMOUNT = "TdAmount";
  private static final String NAME_DISCOUNT = "TdDiscount";
  private static final String NAME_WITHOUT_VAT = "TdWithoutVat";
  private static final String NAME_VAT = "TdVat";
  private static final String NAME_TOTAL = "TdTotal";

  private final TradeDocumentSums tdSums = new TradeDocumentSums();

  TradeDocumentForm() {
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, COL_TRADE_DOCUMENT_PHASE) && widget instanceof TabBar) {
      ((TabBar) widget).addBeforeSelectionHandler(this::onPhaseTransition);

    } else if (BeeUtils.same(name, COL_TRADE_OPERATION) && widget instanceof DataSelector) {
      ((DataSelector) widget).addSelectorHandler(event -> {
        if (event.isOpened()) {
          event.getSelector().setAdditionalFilter(getOperationFilter());
        } else if (event.isChanged()) {
          onOperationChange(event.getRelatedRow());
        }
      });

    } else if (BeeUtils.same(name, GRID_TRADE_DOCUMENT_ITEMS) && widget instanceof ChildGrid) {
      TradeDocumentItemsGrid tdiGrid = new TradeDocumentItemsGrid();

      tdiGrid.setTdsSupplier(() -> tdSums);
      tdiGrid.setTdsListener(this::refreshSums);

      ((ChildGrid) widget).setGridInterceptor(tdiGrid);
    }

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    tdSums.clearItems();

    tdSums.updateDocumentDiscount(getDocumentDiscount(row));

    tdSums.updateDiscountMode(getDiscountMode(row));
    tdSums.updateVatMode(getVatMode(row));

    super.beforeRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new TradeDocumentForm();
  }

  private Double getDocumentDiscount(IsRow row) {
    return DataUtils.getDoubleQuietly(row, getDataIndex(COL_TRADE_DOCUMENT_DISCOUNT));
  }

  private TradeDiscountMode getDiscountMode(IsRow row) {
    return EnumUtils.getEnumByIndex(TradeDiscountMode.class,
        DataUtils.getIntegerQuietly(row, getDataIndex(COL_TRADE_DOCUMENT_DISCOUNT_MODE)));
  }

  private TradeVatMode getVatMode(IsRow row) {
    return EnumUtils.getEnumByIndex(TradeVatMode.class,
        DataUtils.getIntegerQuietly(row, getDataIndex(COL_TRADE_DOCUMENT_VAT_MODE)));
  }

  private Filter getOperationFilter() {
    if (DataUtils.isId(getActiveRowId())) {
      OperationType operationType = getOperationType();
      TradeDocumentPhase phase = getPhase();

      if (operationType != null && phase != null && phase.modifyStock()) {
        return Filter.equals(COL_OPERATION_TYPE, operationType);
      }
    }
    return null;
  }

  private OperationType getOperationType() {
    return EnumUtils.getEnumByIndex(OperationType.class, getIntegerValue(COL_OPERATION_TYPE));
  }

  private TradeDocumentPhase getPhase() {
    return EnumUtils.getEnumByIndex(TradeDocumentPhase.class,
        getIntegerValue(COL_TRADE_DOCUMENT_PHASE));
  }

  private String getShortCaption() {
    String number = getStringValue(COL_TRADE_NUMBER);

    String s1;
    if (BeeUtils.isEmpty(number)) {
      s1 = BeeUtils.joinItems(getStringValue(COL_TRADE_DOCUMENT_NUMBER_1),
          getStringValue(COL_TRADE_DOCUMENT_NUMBER_2));
    } else {
      s1 = BeeUtils.joinWords(getStringValue(COL_SERIES), number);
    }

    return BeeUtils.joinItems(s1, getStringValue(COL_OPERATION_NAME));
  }

  private void onOperationChange(IsRow operationRow) {
    if (operationRow != null) {
      getFormView().updateCell(COL_TRADE_DOCUMENT_PRICE_NAME,
          Data.getString(VIEW_TRADE_OPERATIONS, operationRow, COL_OPERATION_PRICE));

      getFormView().updateCell(COL_TRADE_DOCUMENT_VAT_MODE,
          Data.getString(VIEW_TRADE_OPERATIONS, operationRow, COL_OPERATION_VAT_MODE));

      getFormView().updateCell(COL_TRADE_DOCUMENT_DISCOUNT_MODE,
          Data.getString(VIEW_TRADE_OPERATIONS, operationRow, COL_OPERATION_DISCOUNT_MODE));
    }
  }

  private void onPhaseTransition(BeforeSelectionEvent<Integer> event) {
    final IsRow row = getActiveRow();

    final TradeDocumentPhase from = getPhase();
    final TradeDocumentPhase to = EnumUtils.getEnumByIndex(TradeDocumentPhase.class,
        event.getItem());

    boolean fromStock = from != null && from.modifyStock();
    boolean toStock = to != null && to.modifyStock();

    if (row == null || to == null) {
      event.cancel();

    } else if (fromStock == toStock || DataUtils.isNewRow(row)) {
      setPhase(row, to);

    } else {
      event.cancel();

      String frLabel = (from == null) ? BeeConst.NULL : from.getCaption();
      String toLabel = to.getCaption();
      String message = Localized.dictionary().trdDocumentPhaseTransitionQuestion(frLabel, toLabel);

      Global.confirm(getShortCaption(), Icon.QUESTION, Collections.singletonList(message),
          Localized.dictionary().actionChange(), Localized.dictionary().actionCancel(), () -> {
            if (DataUtils.sameId(row, getActiveRow())) {
              BeeRow newRow = DataUtils.cloneRow(getActiveRow());
              setPhase(newRow, to);

              BeeRowSet rowSet = new BeeRowSet(getViewName(), getFormView().getDataColumns());
              rowSet.addRow(newRow);

              ParameterList params = TradeKeeper.createArgs(SVC_DOCUMENT_PHASE_TRANSITION);
              params.setSummary(getViewName(), newRow.getId());

              BeeKeeper.getRpc().sendText(params, rowSet.serialize(), new ResponseCallback() {
                @Override
                public void onResponse(ResponseObject response) {
                  if (Queries.checkRowResponse(SVC_DOCUMENT_PHASE_TRANSITION, getViewName(),
                      response)) {

                    BeeRow r = BeeRow.restore(response.getResponseAsString());

                    RowUpdateEvent.fire(BeeKeeper.getBus(), getViewName(), r, true);
                    DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_TRADE_STOCK);
                  }
                }
              });
            }
          });
    }
  }

  private void setPhase(IsRow row, TradeDocumentPhase phase) {
    row.setValue(getDataIndex(COL_TRADE_DOCUMENT_PHASE), phase.ordinal());
  }

  private void refreshSum(String name, double value) {
    Widget widget = getFormView().getWidgetByName(name);

    if (widget instanceof DecimalLabel) {
      ((DecimalLabel) widget).setValue(BeeUtils.toDecimalOrNull(value));
    }
  }

  private void refreshSums() {
    double amount = tdSums.getAmount();
    double discount = tdSums.getDiscount();
    double vat = tdSums.getVat();
    double total = tdSums.getTotal();

    refreshSum(NAME_AMOUNT, amount);
    refreshSum(NAME_DISCOUNT, discount);
    refreshSum(NAME_WITHOUT_VAT, total - vat);
    refreshSum(NAME_VAT, vat);
    refreshSum(NAME_TOTAL, total);
  }
}
