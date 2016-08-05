package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.event.logical.RenderingEvent;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.trade.TradeDocumentSums;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.function.Supplier;

public class TradeDocumentItemsGrid extends AbstractGridInterceptor {

  private enum SumColumn {
    AMOUNT {
      @Override
      String getName() {
        return "Amount";
      }

      @Override
      double getValue(IsRow row, TradeDocumentSums tds) {
        return (tds == null) ? BeeConst.DOUBLE_ZERO : tds.getItemAmount(row.getId());
      }
    },

    DISCOUNT {
      @Override
      String getName() {
        return "DiscountAmount";
      }

      @Override
      double getValue(IsRow row, TradeDocumentSums tds) {
        return (tds == null) ? BeeConst.DOUBLE_ZERO : tds.getItemDiscount(row.getId());
      }
    },

    WITHOUT_VAT {
      @Override
      String getName() {
        return "WithoutVat";
      }

      @Override
      double getValue(IsRow row, TradeDocumentSums tds) {
        return (tds == null) ? BeeConst.DOUBLE_ZERO
            : (tds.getItemTotal(row.getId()) - tds.getItemVat(row.getId()));
      }
    },

    VAT {
      @Override
      String getName() {
        return "VatAmount";
      }

      @Override
      double getValue(IsRow row, TradeDocumentSums tds) {
        return (tds == null) ? BeeConst.DOUBLE_ZERO : tds.getItemVat(row.getId());
      }
    },

    TOTAL {
      @Override
      String getName() {
        return "Total";
      }

      @Override
      double getValue(IsRow row, TradeDocumentSums tds) {
        return (tds == null) ? BeeConst.DOUBLE_ZERO : tds.getItemTotal(row.getId());
      }
    };

    abstract String getName();

    abstract double getValue(IsRow row, TradeDocumentSums tds);
  }

  private final class SumRenderer extends AbstractCellRenderer {

    private final SumColumn sumColumn;

    private SumRenderer(SumColumn sumColumn) {
      super(null);
      this.sumColumn = sumColumn;
    }

    @Override
    public String render(IsRow row) {
      if (row == null || tdsSupplier == null) {
        return null;
      } else {
        return BeeUtils.toString(sumColumn.getValue(row, tdsSupplier.get()));
      }
    }
  }

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
  public void afterUpdateCell(IsColumn column, String oldValue, String newValue, IsRow result,
      boolean rowMode) {

    if (column != null && result != null && tdsSupplier != null) {
      boolean fire = false;

      switch (column.getId()) {
        case COL_TRADE_ITEM_QUANTITY:
          fire = tdsSupplier.get().updateQuantity(result.getId(),
              BeeUtils.toDoubleOrNull(newValue));
          break;

        case COL_TRADE_ITEM_PRICE:
          fire = tdsSupplier.get().updatePrice(result.getId(),
              BeeUtils.toDoubleOrNull(newValue));
          break;

        case COL_TRADE_DOCUMENT_ITEM_DISCOUNT:
          fire = tdsSupplier.get().updateDiscount(result.getId(),
              BeeUtils.toDoubleOrNull(newValue));
          break;

        case COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT:
          fire = tdsSupplier.get().updateDiscountIsPercent(result.getId(),
              BeeUtils.toBooleanOrNull(newValue));
          break;

        case COL_TRADE_DOCUMENT_ITEM_VAT:
          fire = tdsSupplier.get().updateVat(result.getId(),
              BeeUtils.toDoubleOrNull(newValue));
          break;

        case COL_TRADE_DOCUMENT_ITEM_VAT_IS_PERCENT:
          fire = tdsSupplier.get().updateVatIsPercent(result.getId(),
              BeeUtils.toBooleanOrNull(newValue));
          break;
      }

      if (fire) {
        fireTdsChange();
      }
    }

    super.afterUpdateCell(column, oldValue, newValue, result, rowMode);
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

  @Override
  public AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription, CellSource cellSource) {

    for (SumColumn sc : SumColumn.values()) {
      if (BeeUtils.same(columnName, sc.getName())) {
        return new SumRenderer(sc);
      }
    }

    return super.getRenderer(columnName, dataColumns, columnDescription, cellSource);
  }

  private void fireTdsChange() {
    if (tdsListener != null) {
      tdsListener.run();
    }
  }
}
