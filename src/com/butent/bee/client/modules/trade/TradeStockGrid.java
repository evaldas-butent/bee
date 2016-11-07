package com.butent.bee.client.modules.trade;

import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.event.logical.RenderingEvent;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;

import java.util.Set;

public class TradeStockGrid extends AbstractGridInterceptor {

  private static final Set<String> showUpdatedColumns =
      Sets.newHashSet(ALS_ITEM_NAME, ALS_WAREHOUSE_CODE, COL_STOCK_QUANTITY);

  private long updatedOn;

  TradeStockGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeStockGrid();
  }

  @Override
  public void afterRender(GridView gridView, RenderingEvent event) {
    if (updatedOn > 0 && gridView != null && !gridView.isEmpty()) {
      for (IsRow row : gridView.getRowData()) {
        if (row.getVersion() > updatedOn && !gridView.getGrid().isRowUpdated(row.getId())) {
          gridView.getGrid().addUpdatedSources(row.getId(), showUpdatedColumns);
        }
      }
    }

    this.updatedOn = System.currentTimeMillis();

    super.afterRender(gridView, event);
  }
}
