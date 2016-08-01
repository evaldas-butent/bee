package com.butent.bee.client.modules.trade;

import com.google.gwt.event.logical.shared.BeforeSelectionEvent;

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
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
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
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collections;

public class TradeDocumentForm extends AbstractFormInterceptor {

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
    }

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public FormInterceptor getInstance() {
    return new TradeDocumentForm();
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
}
