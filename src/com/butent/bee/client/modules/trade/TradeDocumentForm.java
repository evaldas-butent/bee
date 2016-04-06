package com.butent.bee.client.modules.trade;

import com.google.gwt.event.logical.shared.BeforeSelectionEvent;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

public class TradeDocumentForm extends AbstractFormInterceptor {

  TradeDocumentForm() {
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, COL_TRADE_DOCUMENT_PHASE) && widget instanceof TabBar) {
      ((TabBar) widget).addBeforeSelectionHandler(event -> onPhaseTransition(event));
    }

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public FormInterceptor getInstance() {
    return new TradeDocumentForm();
  }

  private TradeDocumentPhase getPhase() {
    return EnumUtils.getEnumByIndex(TradeDocumentPhase.class,
        getIntegerValue(COL_TRADE_DOCUMENT_PHASE));
  }

  private void onPhaseTransition(BeforeSelectionEvent<Integer> event) {
    IsRow row = getActiveRow();

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

    }
  }

  private void setPhase(IsRow row, TradeDocumentPhase phase) {
    row.setValue(getDataIndex(COL_TRADE_DOCUMENT_PHASE), phase.ordinal());
  }
}
