package com.butent.bee.client.modules.orders;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

public class OrdersGrid extends AbstractGridInterceptor {

  @Override
  public GridInterceptor getInstance() {
    return new OrdersGrid();
  }

  @Override
  public DeleteMode beforeDeleteRow(GridPresenter presenter, IsRow row) {
    int index = Data.getColumnIndex(getViewName(), "OrderSale");
    Long orderSales = row.getLong(index);

    if (BeeUtils.isPositive(orderSales)) {
      getGridView().notifySevere(Localized.dictionary().rowIsNotRemovable());
      return DeleteMode.CANCEL;
    } else {
      return DeleteMode.SINGLE;
    }
  }
}