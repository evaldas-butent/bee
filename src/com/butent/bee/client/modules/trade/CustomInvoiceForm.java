package com.butent.bee.client.modules.trade;

import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.PRM_ACCUMULATION_OPERATION;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;

public abstract class CustomInvoiceForm extends PrintFormInterceptor {

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid
        && BeeUtils.inListSame(name, TBL_PURCHASE_ITEMS, TBL_SALE_ITEMS)) {

      ((ChildGrid) widget).setGridInterceptor(new AbstractGridInterceptor() {
        @Override
        public void onEditStart(EditStartEvent event) {
          if (!BeeUtils.same(event.getColumnId(), COL_TRADE_ITEM_ORDINAL)) {
            event.consume();
          } else {
            super.onEditStart(event);
          }
        }

        @Override
        public GridInterceptor getInstance() {
          return null;
        }
      });
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void onReadyForInsert(HasHandlers listener, ReadyForInsertEvent event) {
    if (Objects.equals(Data.getViewTable(getViewName()), TBL_PURCHASES)
        && !Objects.equals(getLongValue(COL_TRADE_OPERATION),
        Global.getParameterRelation(PRM_ACCUMULATION_OPERATION))) {

      for (String col : new String[] {COL_TRADE_INVOICE_NO, COL_TRADE_TERM}) {
        Widget widget = getFormView().getWidgetBySource(col);

        if (widget instanceof Editor && BeeUtils.isEmpty(((Editor) widget).getValue())) {
          getFormView().notifySevere(Data.getColumnLabel(getViewName(), col),
              Localized.dictionary().valueRequired());

          ((Editor) widget).setFocus(true);
          event.consume();
          return;
        }
      }
    }
    super.onReadyForInsert(listener, event);
  }
}
