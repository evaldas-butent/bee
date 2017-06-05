package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
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

  @Override
  public boolean previewCellUpdate(CellUpdateEvent event) {
    long id = event.getRowId();

    if (event.hasSource(COL_TRADE_PAYMENT_AMOUNT)
        && tdsSupplier != null && tdsSupplier.get().containsPayment(id)
        && tdsSupplier.get().updatePayment(id, BeeUtils.toDoubleOrNull(event.getValue()))) {

      fireTdsChange();
    }

    return super.previewCellUpdate(event);
  }

  @Override
  public boolean previewMultiDelete(MultiDeleteEvent event) {
    if (tdsSupplier != null) {
      boolean fire = false;

      for (long id : event.getRowIds()) {
        if (tdsSupplier.get().containsPayment(id)) {
          tdsSupplier.get().deletePayment(id);
          fire = true;
        }
      }

      if (fire) {
        fireTdsChange();
      }
    }

    return super.previewMultiDelete(event);
  }

  @Override
  public boolean previewRowDelete(RowDeleteEvent event) {
    if (tdsSupplier != null && tdsSupplier.get().containsPayment(event.getRowId())) {
      tdsSupplier.get().deletePayment(event.getRowId());
      fireTdsChange();
    }

    return super.previewRowDelete(event);
  }

  private void fireTdsChange() {
    if (tdsListener != null) {
      tdsListener.run();
    }
  }
}
