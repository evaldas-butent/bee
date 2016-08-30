package com.butent.bee.client.modules.orders;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

public class OrdersGrid extends AbstractGridInterceptor {

  @Override
  public GridInterceptor getInstance() {
    return new OrdersGrid();
  }

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {
    int index = Data.getColumnIndex(getViewName(), COL_ORDER);

    if (index < 0) {
      return null;
    }

    Long orderId = activeRow.getLong(index);

    if (BeeUtils.isPositive(orderId)) {
      getGridView().notifySevere(Localized.dictionary().rowIsNotRemovable());
      return DeleteMode.CANCEL;
    } else {
      return DeleteMode.SINGLE;
    }
  }
}