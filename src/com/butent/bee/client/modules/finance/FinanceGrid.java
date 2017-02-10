package com.butent.bee.client.modules.finance;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

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

  @Override
  public boolean validateRow(IsRow row, NotificationListener notificationListener) {
    if (row != null) {
      Long debit = row.getLong(getDataIndex(COL_FIN_DEBIT));
      Long credit = row.getLong(getDataIndex(COL_FIN_CREDIT));

      if (Objects.equals(debit, credit)) {
        if (notificationListener != null) {
          notificationListener.notifySevere(BeeUtils.joinWords(Localized.dictionary().debit(),
              BeeConst.STRING_EQ, Localized.dictionary().credit()));
        }
        return false;
      }
    }

    return super.validateRow(row, notificationListener);
  }
}
