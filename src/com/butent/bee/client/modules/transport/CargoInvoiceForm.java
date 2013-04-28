package com.butent.bee.client.modules.transport;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

public class CargoInvoiceForm extends AbstractFormInterceptor {

  private Widget detailsWidget = null;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid && BeeUtils.same(name, TBL_SALE_ITEMS)) {
      ((ChildGrid) widget).setGridInterceptor(new AbstractGridInterceptor() {
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
          if (BeeUtils.inListSame(column.getId(), COL_QUANTITY, COL_PRICE)) {
            refreshTotals();
          }
        }

        private void refreshTotals() {
          final FormView form = getFormView();

          Queries.getRow(form.getViewName(), form.getActiveRow().getId(), new RowCallback() {
            @Override
            public void onSuccess(BeeRow result) {
              form.updateRow(result, false);
            }
          });
        }
      });
    } else if (BeeUtils.same(name, "InvoiceDetails")) {
      detailsWidget = widget.asWidget();
    }
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == Action.PRINT) {
      String view = "PrintCargoInvoice";

      RowEditor.openRow(view, Data.getDataInfo(view),
          getFormView().getActiveRow().getId(), true, null, null);
      return false;
    }
    return super.beforeAction(action, presenter);
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoInvoiceForm();
  }
}
