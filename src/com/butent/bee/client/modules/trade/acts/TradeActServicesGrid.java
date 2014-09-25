package com.butent.bee.client.modules.trade.acts;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.trade.Totalizer;
import com.butent.bee.shared.utils.BeeUtils;

public class TradeActServicesGrid extends AbstractGridInterceptor {

  TradeActServicesGrid() {
  }

  @Override
  public void afterUpdateCell(IsColumn column, String oldValue, String newValue, IsRow result,
      boolean rowMode) {

    if (COL_TA_SERVICE_TARIFF.equalsIgnoreCase(column.getId())
        && BeeUtils.isPositiveDouble(newValue)) {

      GridView items = ViewHelper.getSiblingGrid(getGridView().asWidget(), GRID_TRADE_ACT_ITEMS);

      if (items != null && !items.isEmpty()) {
        Totalizer totalizer = new Totalizer(items.getDataColumns());

        int qtyIndex = items.getDataIndex(COL_TRADE_ITEM_QUANTITY);
        totalizer.setQuantityFuction(new QuantityReader(qtyIndex));

        double total = BeeConst.DOUBLE_ZERO;
        for (IsRow row : items.getRowData()) {
          Double amount = totalizer.getTotal(row);
          if (BeeUtils.isDouble(amount)) {
            total += amount;
          }
        }

        if (BeeUtils.isPositive(total)) {
          double price = Data.round(getViewName(), COL_TRADE_ITEM_PRICE,
              BeeUtils.percent(total, BeeUtils.toDouble(newValue)));

          if (BeeUtils.isPositive(price)) {
            IsRow row = getGridView().getGrid().getRowById(result.getId());
            String oldPrice = (row == null)
                ? null : row.getString(getDataIndex(COL_TRADE_ITEM_PRICE));

            String newPrice = BeeUtils.toString(price);

            Queries.updateCellAndFire(getViewName(), result.getId(), result.getVersion(),
                COL_TRADE_ITEM_PRICE, oldPrice, newPrice);
          }
        }
      }
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeActServicesGrid();
  }
}
