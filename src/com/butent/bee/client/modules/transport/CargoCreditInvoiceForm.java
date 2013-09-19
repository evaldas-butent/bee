package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.trade.TradeConstants.TBL_PURCHASE_ITEMS;
import static com.butent.bee.shared.modules.transport.TransportConstants.VIEW_CARGO_CREDIT_INCOMES;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

public class CargoCreditInvoiceForm extends AbstractFormInterceptor {

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid) {
      ChildGrid grid = (ChildGrid) widget;

      if (BeeUtils.same(name, TBL_PURCHASE_ITEMS)) {
        grid.setGridInterceptor(new InvoiceItemsGrid(getFormView()));

      } else if (BeeUtils.same(name, VIEW_CARGO_CREDIT_INCOMES)) {
        grid.setGridInterceptor(new AbstractGridInterceptor());
      }
    }
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == Action.PRINT) {
      String print = DomUtils.getData(getFormView().getRootWidget().getElement(), "printing");

      if (!BeeUtils.isEmpty(print)) {
        final String[] reports = BeeUtils.split(print, BeeConst.CHAR_COMMA);

        ChoiceCallback choice = new ChoiceCallback() {
          @Override
          public void onSuccess(int value) {
            RowEditor.openRow(reports[value], Data.getDataInfo(getFormView().getViewName()),
                getFormView().getActiveRow(), true, null, null, null,
                new PrintInvoiceInterceptor());
          }
        };
        if (reports.length > 1) {
          Global.choice(Localized.getConstants().trInvoice(),
              Localized.getConstants().choosePrintingForm(), Lists.newArrayList(reports), choice);
        } else {
          choice.onSuccess(0);
        }
        return false;
      }
    }
    return super.beforeAction(action, presenter);
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoCreditInvoiceForm();
  }
}
