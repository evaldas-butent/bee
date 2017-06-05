package com.butent.bee.client.modules.orders;

import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.modules.classifiers.ItemsPicker;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;

public class OrderTmplItemsPicker extends ItemsPicker {

  @Override
  public void getItems(Filter filter, final RowSetCallback callback) {
    ParameterList params = OrdersKeeper.createSvcArgs(SVC_GET_TMPL_ITEMS_FOR_SELECTION);

    if (filter != null) {
      params.addDataItem(Service.VAR_VIEW_WHERE, filter.serialize());
    }

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(BeeRowSet.class)) {
          callback.onSuccess(BeeRowSet.restore(response.getResponseAsString()));
        } else {
          BeeKeeper.getScreen().notifyWarning(Localized.dictionary().nothingFound());
          getSpinner().setStyleName(STYLE_SEARCH_SPINNER_LOADING, false);
        }
      }
    });
  }

  @Override
  public Long getWarehouseFrom(IsRow row) {
    return null;
  }

  @Override
  public boolean setIsOrder(IsRow row) {
    return false;
  }

}
