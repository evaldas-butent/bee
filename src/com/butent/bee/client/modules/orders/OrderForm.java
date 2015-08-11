package com.butent.bee.client.modules.orders;

import static com.butent.bee.shared.modules.orders.OrdersConstants.COL_ORDERS_STATUS;
import static com.butent.bee.shared.modules.orders.OrdersConstants.TBL_ORDER_ITEMS;
import static com.butent.bee.shared.modules.orders.OrdersConstants.VIEW_ORDERS;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.orders.OrdersConstants.OrdersStatus;
import com.butent.bee.shared.utils.BeeUtils;

public class OrderForm extends AbstractFormInterceptor {

  @Override
  public FormInterceptor getInstance() {
    return new OrderForm();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (BeeUtils.same(name, TBL_ORDER_ITEMS)) {
      ((ChildGrid) widget).setGridInterceptor(new OrderItemsGrid());
    }
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    final int idxStatus = form.getDataIndex(COL_ORDERS_STATUS);

    GridView parentGrid = getGridView();
    if (parentGrid == null) {
      return;
    } else if (parentGrid.getGridName() == VIEW_ORDERS
        && row.getInteger(idxStatus) == OrdersStatus.PREPARED.ordinal()) {
      updateStatus(form, OrdersStatus.APPROVED);
    }

    boolean isOrder =
        (row.getInteger(idxStatus) == OrdersStatus.APPROVED.ordinal()) ? true : false;
    String caption;

    if (DataUtils.isNewRow(row)) {
      caption = isOrder
          ? Localized.getConstants().newOrder() : Localized.getConstants().newOffer();
    } else {
      caption = isOrder
          ? Localized.getConstants().order() : Localized.getConstants().offer();
    }

    if (!BeeUtils.isEmpty(caption)) {
      header.setCaption(caption);
    }
  }

  private static void updateStatus(FormView form, OrdersStatus status) {
    form.getActiveRow().setValue(Data.getColumnIndex(VIEW_ORDERS, COL_ORDERS_STATUS),
        status.ordinal());
    form.refreshBySource(COL_ORDERS_STATUS);
  }
}
