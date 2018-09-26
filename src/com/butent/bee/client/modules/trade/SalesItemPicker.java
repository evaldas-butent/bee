package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.SVC_GET_ITEMS_FOR_SELECTION;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.modules.classifiers.ItemsPicker;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;

public class SalesItemPicker extends ItemsPicker {

  private static final String STYLE_STOCK_PREFIX = STYLE_PREFIX + "stock-";
  private static final String STYLE_STOCK_POSITIVE = STYLE_STOCK_PREFIX + "positive";

  @Override
  public void getItems(Filter filter, Queries.RowSetCallback callback) {
    ParameterList params = TradeKeeper.createArgs(SVC_GET_ITEMS_FOR_SELECTION);

    if (DataUtils.hasId(getLastRow())) {
      params.addDataItem(COL_SALE, getLastRow().getId());
    }

    if (DataUtils.isId(getWarehouseFrom())) {
      params.addDataItem(ClassifierConstants.COL_WAREHOUSE, getWarehouseFrom());
    }

    if (filter != null) {
      params.addDataItem(Service.VAR_VIEW_WHERE, filter.serialize());
    }

    params.addDataItem(Service.VAR_TABLE, getSource());

    BeeKeeper.getRpc().makeRequest(params, response -> {
      if (response.hasResponse(BeeRowSet.class)) {
        callback.onSuccess(BeeRowSet.restore(response.getResponseAsString()));
      } else {
        getSpinner().setStyleName(STYLE_SEARCH_SPINNER_LOADING, false);
        BeeKeeper.getScreen().notifyWarning(Localized.dictionary().nothingFound());
      }
    });
  }

  @Override
  public Long getWarehouseFrom(IsRow row) {
    int warehouseIdx = Data.getColumnIndex(VIEW_SALES, COL_TRADE_WAREHOUSE_FROM);
    if (row == null || BeeConst.isUndef(warehouseIdx)) {
      return null;
    }

    return row.getLong(warehouseIdx);
  }

  @Override
  public boolean setIsOrder(IsRow row) {
    return false;
  }

  @Override
  protected String getCaption() {
    return Localized.dictionary().goods();
  }

  protected String getSource() {
    return TBL_SALE_ITEMS;
  }
}
