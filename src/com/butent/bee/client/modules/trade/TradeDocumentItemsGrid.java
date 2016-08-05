package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.event.logical.RenderingEvent;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.trade.TradeDocumentSums;

import java.util.function.Supplier;

public class TradeDocumentItemsGrid extends AbstractGridInterceptor {

  private Supplier<TradeDocumentSums> tdsSupplier;
  private Runnable tdsListener;

  TradeDocumentItemsGrid() {
  }

  void setTdsSupplier(Supplier<TradeDocumentSums> tdsSupplier) {
    this.tdsSupplier = tdsSupplier;
  }

  void setTdsListener(Runnable tdsListener) {
    this.tdsListener = tdsListener;
  }

  @Override
  public void beforeRender(GridView gridView, RenderingEvent event) {
    if (tdsSupplier != null && !gridView.isEmpty()) {
      int qtyIndex = getDataIndex(COL_TRADE_ITEM_QUANTITY);
      int priceIndex = getDataIndex(COL_TRADE_ITEM_PRICE);

      int discountIndex = getDataIndex(COL_TRADE_DOCUMENT_ITEM_DISCOUNT);
      int dipIndex = getDataIndex(COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT);

      int vatIndex = getDataIndex(COL_TRADE_DOCUMENT_ITEM_VAT);
      int vipIndex = getDataIndex(COL_TRADE_DOCUMENT_ITEM_VAT_IS_PERCENT);

      for (IsRow row : gridView.getRowData()) {
        tdsSupplier.get().add(row.getId(), row.getDouble(qtyIndex), row.getDouble(priceIndex),
            row.getDouble(discountIndex), row.getBoolean(dipIndex),
            row.getDouble(vatIndex), row.getBoolean(vipIndex));
      }

      fireTdsChange();
    }

    super.beforeRender(gridView, event);
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeDocumentItemsGrid();
  }

  private void fireTdsChange() {
    if (tdsListener != null) {
      tdsListener.run();
    }
  }
}
