package com.butent.bee.client.modules.transport;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Objects;

public class CargoInvoiceChecker extends CargoTripChecker {

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {

    Provider provider = presenter.getDataProvider();
    Long manager = activeRow.getLong(provider.getColumnIndex(TransportConstants.COL_ORDER_MANAGER));

    if (Objects.equals(manager, BeeKeeper.getUser().getUserId())
        || BeeKeeper.getUser().isAdministrator()) {
      ParameterList args = createArgs(presenter, activeRow, selectedRows);
      args.addDataItem(Service.VAR_COLUMN, TradeConstants.COL_SALE);

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          response.notify(presenter.getGridView());

          if (!response.hasErrors()) {
            if (BeeUtils.isPositive(response.getResponseAsInt())) {
              Global.showError(Localized.dictionary().rowIsReadOnly());
            } else {
              CargoInvoiceChecker.super.getDeleteMode(presenter, activeRow, selectedRows, defMode);
            }
          }
        }
      });
    } else {
      presenter.getGridView().notifyWarning(
          Localized.dictionary().trTransportationOrderDeleteCanManager());
    }
    return DeleteMode.CANCEL;
  }

  @Override
  public GridInterceptor getInstance() {
    return new CargoInvoiceChecker();
  }
}
