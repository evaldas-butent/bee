package com.butent.bee.client.modules.orders;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Collections;

public class OrdersGrid extends AbstractGridInterceptor {

  @Override
  public GridInterceptor getInstance() {
    return new OrdersGrid();
  }

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {

    Queries.getRowCount(VIEW_ORDER_CHILD_INVOICES, Filter.equals(COL_ORDER, getActiveRowId()),
        new Queries.IntCallback() {
          @Override
          public void onSuccess(Integer result) {
            if (BeeUtils.isPositive(result)) {
              getGridView().notifySevere(Localized.dictionary().rowIsNotRemovable());
            } else {
              Global.confirm(Localized.dictionary().orders(), Icon.WARNING,
                  Collections.singletonList(Localized.dictionary().deleteRowQuestion()), () -> {
                    Queries.deleteRow(VIEW_ORDERS, getActiveRowId());
                    DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_ORDERS);
                  });
            }
          }
        });
    return DeleteMode.CANCEL;
  }
}