package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.HasRowValue;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.DecimalValue;
import com.butent.bee.shared.data.value.Value;
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

  private final class SumRenderer extends AbstractCellRenderer implements HasRowValue {

    private final SumColumn sumColumn;

    private SumRenderer(SumColumn sumColumn) {
      super(null);
      this.sumColumn = sumColumn;
    }

    @Override
    public boolean dependsOnSource(String source) {
      return BeeUtils.inList(source, COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE,
          COL_TRADE_DOCUMENT_ITEM_DISCOUNT, COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT,
          COL_TRADE_DOCUMENT_ITEM_VAT, COL_TRADE_DOCUMENT_ITEM_VAT_IS_PERCENT);
    }

    @Override
    public Value getRowValue(IsRow row) {
      return DecimalValue.of(evaluate(row));
    }

    @Override
    public String render(IsRow row) {
      double x = evaluate(row);
      return (x == BeeConst.DOUBLE_ZERO) ? null : BeeUtils.toString(x);
    }

    private double evaluate(IsRow row) {
      if (row == null || tdsSupplier == null) {
        return BeeConst.DOUBLE_ZERO;
      } else {
        return sumColumn.getValue(row, tdsSupplier.get());
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
  public void afterDeleteRow(long rowId) {
    if (tdsSupplier != null && tdsSupplier.get().delete(rowId)) {
      fireTdsChange();
    }

    super.afterDeleteRow(rowId);
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

  @Override
  public void onDataReceived(List<? extends IsRow> rows) {
    if (tdsSupplier != null) {
      if (getGridPresenter() != null && getGridPresenter().getUserFilter() == null) {
        tdsSupplier.get().clearItems();
      }

      if (!BeeUtils.isEmpty(rows)) {
        int qtyIndex = getDataIndex(COL_TRADE_ITEM_QUANTITY);
        int priceIndex = getDataIndex(COL_TRADE_ITEM_PRICE);

        int discountIndex = getDataIndex(COL_TRADE_DOCUMENT_ITEM_DISCOUNT);
        int dipIndex = getDataIndex(COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT);

        int vatIndex = getDataIndex(COL_TRADE_DOCUMENT_ITEM_VAT);
        int vipIndex = getDataIndex(COL_TRADE_DOCUMENT_ITEM_VAT_IS_PERCENT);

        for (IsRow row : rows) {
          tdsSupplier.get().add(row.getId(), row.getDouble(qtyIndex), row.getDouble(priceIndex),
              row.getDouble(discountIndex), row.getBoolean(dipIndex),
              row.getDouble(vatIndex), row.getBoolean(vipIndex));
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
