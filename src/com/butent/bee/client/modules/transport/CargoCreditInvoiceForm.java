package com.butent.bee.client.modules.transport;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.output.PrintFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.event.DataChangeEvent.Effect;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.EnumSet;

public class CargoCreditInvoiceForm extends PrintFormInterceptor
    implements ValueChangeHandler<String> {

  private ScheduledCommand refresher;

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {
    if (BeeUtils.same(editableWidget.getColumnId(), COL_TRADE_VAT_INCL)
        && widget instanceof InputBoolean) {
      ((InputBoolean) widget).addValueChangeHandler(this);
    }
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid) {
      ChildGrid grid = (ChildGrid) widget;

      if (BeeUtils.same(name, getTradeItemsName())) {
        grid.setGridInterceptor(new InvoiceItemsGrid(getRefresher()));

      } else if (BeeUtils.inListSame(name, VIEW_CARGO_CREDIT_INCOMES, VIEW_CARGO_INVOICE_INCOMES)) {
        grid.setGridInterceptor(new AbstractGridInterceptor());
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

  @Override
  public void onValueChange(ValueChangeEvent<String> event) {
    Queries.update(getFormView().getViewName(),
        ComparisonFilter.compareId(getFormView().getActiveRow().getId()),
        COL_TRADE_VAT_INCL, new BooleanValue(BeeUtils.toBoolean(event.getValue())),
        new IntCallback() {
          @Override
          public void onSuccess(Integer result) {
            if (BeeUtils.isPositive(result)) {
              getRefresher().execute();
              Data.onTableChange(getTradeItemsName(), EnumSet.of(Effect.REFRESH));
            }
          }
        });
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
              BeeKeeper.getBus().fireEvent(new RowUpdateEvent(form.getViewName(), result));
            }
          });
        }
      };
    }
    return refresher;
  }
}
