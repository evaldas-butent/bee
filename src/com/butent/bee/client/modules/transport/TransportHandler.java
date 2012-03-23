package com.butent.bee.client.modules.transport;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.grid.AbstractColumn;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.utils.AbstractEvaluation;
import com.butent.bee.client.utils.Evaluator.Parameters;
import com.butent.bee.client.utils.HasEvaluation;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.modules.transport.TransportConstants.OrderStatus;
import com.butent.bee.shared.utils.BeeUtils;

public class TransportHandler {

  private static class OrderFormHandler extends AbstractFormCallback {
    private Widget status = null;

    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (BeeUtils.same(name, TransportConstants.COL_STATUS)) {
        status = widget;
      }
    }

    @Override
    public void afterRefresh(FormView form, IsRow row) {
      String text = BeeUtils.getName(OrderStatus.class,
          row.getInteger(form.getDataIndex(TransportConstants.COL_STATUS)));

      status.getElement().setInnerText(text);
    }

    @Override
    public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
      newRow.setValue(form.getDataIndex("Date"), System.currentTimeMillis());
      newRow.setValue(form.getDataIndex(TransportConstants.COL_STATUS),
          OrderStatus.CREATED.ordinal());
    }
  }

  private static class OrdersGridHandler extends AbstractGridCallback {
    @Override
    public boolean afterCreateColumn(String columnId, AbstractColumn<?> column,
        ColumnHeader header, ColumnFooter footer) {

      if (BeeUtils.same(columnId, TransportConstants.COL_STATUS) && column instanceof HasEvaluation) {
        ((HasEvaluation) column).setEvaluation(new AbstractEvaluation() {
          @Override
          public String eval(Parameters parameters) {
            return BeeUtils.getName(OrderStatus.class,
                parameters.getInteger(TransportConstants.COL_STATUS));
          }
        });
      }
      return true;
    }
  }

  public static void register() {
    GridFactory.registerGridCallback("TripOrders", new OrdersGridHandler());
    FormFactory.registerFormCallback("TripOrder", new OrderFormHandler());
  }

  private TransportHandler() {
  }

}
