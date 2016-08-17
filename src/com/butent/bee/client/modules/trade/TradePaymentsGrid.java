package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.trade.TradeDocumentSums;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.function.Supplier;

public class TradePaymentsGrid extends AbstractGridInterceptor {

  private Supplier<TradeDocumentSums> tdsSupplier;
  private Runnable tdsListener;

  TradePaymentsGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradePaymentsGrid();
  }

  void setTdsSupplier(Supplier<TradeDocumentSums> tdsSupplier) {
    this.tdsSupplier = tdsSupplier;
  }

  void setTdsListener(Runnable tdsListener) {
    this.tdsListener = tdsListener;
  }

  @Override
  public void afterDeleteRow(long rowId) {
    if (tdsSupplier != null && tdsSupplier.get().deletePayment(rowId)) {
      fireTdsChange();
    }

    super.afterDeleteRow(rowId);
  }

  @Override
  public void afterUpdateCell(IsColumn column, String oldValue, String newValue, IsRow result,
      boolean rowMode) {

    if (column != null && column.getId().equals(COL_TRADE_PAYMENT_AMOUNT)
        && DataUtils.hasId(result) && tdsSupplier != null
        && tdsSupplier.get().updatePayment(result.getId(), BeeUtils.toDoubleOrNull(newValue))) {

      fireTdsChange();
    }

    super.afterUpdateCell(column, oldValue, newValue, result, rowMode);
  }

  @Override
  public void onDataReceived(List<? extends IsRow> rows) {
    if (tdsSupplier != null) {
      if (getGridPresenter() != null && getGridPresenter().getUserFilter() == null) {
        tdsSupplier.get().clearPayments();
      }

      if (!BeeUtils.isEmpty(rows)) {
        int index = getDataIndex(COL_TRADE_PAYMENT_AMOUNT);

        for (IsRow row : rows) {
          tdsSupplier.get().addPayment(row.getId(), row.getDouble(index));
        }
      }

      fireTdsChange();
    }

    super.onDataReceived(rows);
  }

  private void fireTdsChange() {
    if (tdsListener != null) {
      tdsListener.run();
    }
  }
}
