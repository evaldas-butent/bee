package com.butent.bee.client.modules.transport;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import static com.butent.bee.shared.modules.trade.TradeConstants.TBL_PURCHASE_ITEMS;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.output.PrintFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.utils.BeeUtils;

public class CargoCreditInvoiceForm extends PrintFormInterceptor {

  private ScheduledCommand refresher;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid) {
      ChildGrid grid = (ChildGrid) widget;

      if (BeeUtils.same(name, getTradeItemsName())) {
        grid.setGridInterceptor(new InvoiceItemsGrid(getRefresher()));

      } else if (BeeUtils.inListSame(name, VIEW_CARGO_CREDIT_INCOMES, VIEW_CARGO_INVOICE_INCOMES)) {
        grid.setGridInterceptor(new AbstractGridInterceptor() /* Kill default interceptor */);
      }
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoCreditInvoiceForm();
  }

  @Override
  public FormInterceptor getPrintFormInterceptor() {
    return new PrintInvoiceInterceptor();
  }

  protected String getTradeItemsName() {
    return TBL_PURCHASE_ITEMS;
  }

  private ScheduledCommand getRefresher() {
    if (refresher == null) {
      refresher = new ScheduledCommand() {
        @Override
        public void execute() {
          final FormView form = getFormView();

          Queries.getRow(form.getViewName(), form.getActiveRow().getId(), new RowCallback() {
            @Override
            public void onSuccess(BeeRow result) {
              RowUpdateEvent.fire(BeeKeeper.getBus(), form.getViewName(), result);
            }
          });
        }
      };
    }
    return refresher;
  }
}
