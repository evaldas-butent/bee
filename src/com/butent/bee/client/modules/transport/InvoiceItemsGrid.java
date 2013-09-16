package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.utils.BeeUtils;

public class InvoiceItemsGrid extends AbstractGridInterceptor {
  private final FormView form;

  public InvoiceItemsGrid(FormView form) {
    this.form = form;
  }

  @Override
  public void afterDeleteRow(long rowId) {
    refreshTotals();
  }

  @Override
  public void afterInsertRow(IsRow result) {
    refreshTotals();
  }

  @Override
  public void afterUpdateCell(IsColumn column, IsRow result, boolean rowMode) {
    if (BeeUtils.inListSame(column.getId(), COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE,
        COL_TRADE_ITEM_VAT, COL_TRADE_ITEM_VAT_PERC)) {
      refreshTotals();
    }
  }

  private void refreshTotals() {
    Queries.getRow(form.getViewName(), form.getActiveRow().getId(), new RowCallback() {
      @Override
      public void onSuccess(BeeRow result) {
        form.updateRow(result, false);
        BeeKeeper.getBus().fireEvent(new RowUpdateEvent(form.getViewName(), result));
      }
    });
  }
}
