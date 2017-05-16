package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.modules.trade.DebtKind;

class PaymentForm extends AbstractFormInterceptor {

  private static final String NAME_PAYER = "Payer";

  private final DebtKind debtKind;

  PaymentForm(DebtKind debtKind) {
    this.debtKind = debtKind;
  }

  @Override
  public FormInterceptor getInstance() {
    return new PaymentForm(debtKind);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (NAME_PAYER.equals(name) && widget instanceof DataSelector) {
      ((DataSelector) widget).addSelectorHandler(event -> {
        if (event.isChanged()) {
          onPayerChange(event.getValue());
        }
      });
    }

    super.afterCreateWidget(name, widget, callback);
  }

  private void onPayerChange(Long payer) {
    refreshDebts(GRID_TRADE_PAYABLES, payer);
    refreshDebts(GRID_TRADE_RECEIVABLES, payer);
  }

  private void refreshDebts(String gridName, Long payer) {
    GridView gridView = ViewHelper.getChildGrid(getFormView(), gridName);

    if (gridView != null && gridView.getGridInterceptor() instanceof TradeDebtsGrid) {
      ((TradeDebtsGrid) gridView.getGridInterceptor()).onCompanyChange(payer);
    }
  }
}
