package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;
import java.util.Collections;

public class AccumulationsGrid extends AbstractGridInterceptor {

  @Override
  public GridInterceptor getInstance() {
    return new AccumulationsGrid();
  }

  public static void preload(Runnable command) {
    Long id = Global.getParameterRelation(PRM_ACCUMULATION_OPERATION);

    if (!DataUtils.isId(id)) {
      Global.showError(Arrays.asList("Nenurodyta sukaupimų operacija", PRM_ACCUMULATION_OPERATION));
    } else {
      GridFactory.registerImmutableFilter(VIEW_ACCUMULATIONS,
          Filter.equals(TradeConstants.COL_TRADE_OPERATION, id));
      command.run();
    }
  }

  @Override
  public void afterUpdateCell(IsColumn column, String oldValue, String newValue, IsRow result,
      boolean rowMode) {

    String col;

    switch (column.getId()) {
      case TradeConstants.COL_TRADE_ITEM_PRICE:
        col = COL_AMOUNT;
        break;
      case TradeConstants.COL_TRADE_VAT_PLUS:
      case TradeConstants.COL_TRADE_VAT:
      case TradeConstants.COL_TRADE_VAT_PERC:
        col = column.getId();
        break;
      default:
        col = null;
        break;
    }
    if (!BeeUtils.isEmpty(col) && rowMode) {
      Queries.getRow(TBL_CARGO_EXPENSES,
          Filter.compareId(result.getLong(getDataIndex("CargoExpense"))),
          Collections.singletonList(col), new RowCallback() {
            @Override
            public void onSuccess(BeeRow row) {
              Queries.updateCellAndFire(TBL_CARGO_EXPENSES, row.getId(),
                  row.getVersion(), col, row.getString(0), newValue);
            }
          });
    }
    super.afterUpdateCell(column, oldValue, newValue, result, rowMode);
  }
}
