package com.butent.bee.client.modules.transport;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.COL_COSTS_VAT;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridFormKind;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.ParentRowRefreshGrid;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.utils.BeeUtils;

public abstract class TransportVatGridInterceptor extends ParentRowRefreshGrid {

  @Override
  public void onLoad(GridView gridView) {
    super.onLoad(gridView);
    FormView form = gridView.getForm(GridFormKind.NEW_ROW);

    if (form != null) {
      Widget vatPlusWidget = form.getWidgetBySource(TradeConstants.COL_TRADE_VAT_PLUS);

      if (vatPlusWidget instanceof InputBoolean) {
        ((InputBoolean) vatPlusWidget).addValueChangeHandler(valueChangeEvent -> {
          int vatColumnIndex = Data.getColumnIndex(getViewName(), COL_COSTS_VAT);
          String vat = form.getActiveRow().getString(vatColumnIndex);

          if (BeeUtils.toBoolean(valueChangeEvent.getValue()) && BeeUtils.isEmpty(vat)) {
            Global.getParameter(ClassifierConstants.COL_ITEM_VAT_PERCENT, input -> {
              Double defaultVatPercent = BeeUtils.toDoubleOrNull(input);

              getActiveRow().setValue(vatColumnIndex, defaultVatPercent);
              getActiveRow().setValue(Data.getColumnIndex(getViewName(),
                  ClassifierConstants.COL_ITEM_VAT_PERCENT), true);

              form.refreshBySource(COL_COSTS_VAT);
              form.refreshBySource(ClassifierConstants.COL_ITEM_VAT_PERCENT);
            });
          }
        });
      }
    }
  }
}
