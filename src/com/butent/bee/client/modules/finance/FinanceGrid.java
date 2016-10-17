package com.butent.bee.client.modules.finance;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.shared.BeeConst;

import java.util.Objects;

abstract class FinanceGrid extends AbstractGridInterceptor {

  protected FinanceGrid() {
  }

  @Override
  public void onReadyForUpdate(GridView gridView, ReadyForUpdateEvent event) {
    if (event != null && event.getRowValue() != null && event.getColumn() != null) {
      String opposite;

      switch (event.getColumnId()) {
        case COL_FIN_DEBIT:
          opposite = COL_FIN_CREDIT;
          break;

        case COL_FIN_CREDIT:
          opposite = COL_FIN_DEBIT;
          break;

        default:
          opposite = null;
      }

      int index = (opposite == null) ? BeeConst.UNDEF : getDataIndex(opposite);
      if (!BeeConst.isUndef(index)
          && Objects.equals(event.getNewValue(), event.getRowValue().getString(index))) {

        event.getRowValue().preliminaryUpdate(index, event.getOldValue());
      }
    }

    super.onReadyForUpdate(gridView, event);
  }
}
