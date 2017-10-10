package com.butent.bee.client.modules.transport;

import com.butent.bee.client.modules.trade.TradeUtils;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.modules.trade.TradeConstants;

import java.util.Objects;

public class TripFuelCostsGrid extends TransportVatGridInterceptor {

  @Override
  public GridInterceptor getInstance() {
    return new TripFuelCostsGrid();
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (Objects.equals(event.getColumnId(), TradeConstants.VAR_TOTAL)) {
      event.consume();
      TradeUtils.amountEntry(event.getRowValue(), getViewName());
      return;
    }
    super.onEditStart(event);
  }
}
