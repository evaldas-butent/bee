package com.butent.bee.client.modules.orders;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.modules.trade.InvoicesGrid;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.time.DateTime;

import java.util.Collection;
import java.util.Set;

public class OrdersInvoicesGrid extends InvoicesGrid {

  @Override
  public GridInterceptor getInstance() {
    return this;
  }

  @Override
  public void getERPStocks(final Set<Long> ids) {
    ParameterList params = OrdersKeeper.createSvcArgs(SVC_GET_ERP_STOCKS);
    params.addDataItem(Service.VAR_DATA, DataUtils.buildIdList(ids));

    BeeKeeper.getRpc().makeRequest(params);
  }

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {

    int exportedIdx = Data.getColumnIndex(getViewName(), TradeConstants.COL_TRADE_EXPORTED);

    if (exportedIdx < 0) {
      return null;
    }

    DateTime exported = activeRow.getDateTime(exportedIdx);

    if (BeeKeeper.getUser().isAdministrator()) {
      return DeleteMode.SINGLE;
    } else if (exported != null) {
      getGridView().notifySevere(Localized.dictionary().rowIsNotRemovable());
      return DeleteMode.CANCEL;
    } else {
      return DeleteMode.SINGLE;
    }
  }
}