package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.ProvidesGridColumnRenderer;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasRowValue;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.DecimalValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.export.XCell;
import com.butent.bee.shared.export.XFont;
import com.butent.bee.shared.export.XSheet;
import com.butent.bee.shared.export.XStyle;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TotalRenderer extends AbstractCellRenderer implements HasRowValue {

  public static class Provider implements ProvidesGridColumnRenderer {
    @Override
    public AbstractCellRenderer getRenderer(String columnName,
        List<? extends IsColumn> dataColumns, ColumnDescription columnDescription,
        CellSource cellSource) {

      return new TotalRenderer(dataColumns);
    }
  }

  private static final String[] COL_NAMES = new String[] {
      COL_TRADE_AMOUNT, COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE,
      COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC,
      COL_TRADE_DISCOUNT};

  private final Map<String, Integer> data = new HashMap<>();

  public TotalRenderer(List<? extends IsColumn> columns) {
    super(null);

    for (String colName : COL_NAMES) {
      int idx = DataUtils.getColumnIndex(colName, columns);

      if (!BeeConst.isUndef(idx)) {
        data.put(colName, idx);
      }
    }
  }

  @Override
  public boolean dependsOnSource(String source) {
    return !BeeUtils.isEmpty(source) && data.containsKey(source);
  }

  @Override
  public XCell export(IsRow row, int cellIndex, Integer styleRef, XSheet sheet) {
    Double v = evaluate(row);
    return (v == null) ? null : new XCell(cellIndex, v, styleRef);
  }

  public Double getDiscount(IsRow row) {
    if (row == null) {
      return null;
    } else {
      return getDiscount(row, getAmount(row));
    }
  }

  @Override
  public Value getRowValue(IsRow row) {
    Double v = evaluate(row);
    return (v == null) ? null : DecimalValue.of(v);
  }

  public Double getTotal(IsRow row) {
    if (row == null) {
      return null;
    }

    Double amount = getAmount(row);
    if (!BeeUtils.isDouble(amount)) {
      return null;
    }

    Double discount = getDiscount(row, amount);
    if (BeeUtils.isDouble(discount)) {
      amount -= discount;
    }

    if (data.containsKey(COL_TRADE_VAT_PLUS)
        && BeeUtils.isTrue(row.getBoolean(data.get(COL_TRADE_VAT_PLUS)))) {

      Double vat = getVat(row, amount);
      if (BeeUtils.isDouble(vat)) {
        amount += vat;
      }
    }

    return amount;
  }

  @Override
  public ValueType getValueType() {
    return ValueType.DECIMAL;
  }

  public Double getVat(IsRow row) {
    if (row == null) {
      return null;

    } else {
      Double base = getAmount(row);

      if (base == null) {
        return null;

      } else {
        Double discount = getDiscount(row, base);
        if (BeeUtils.isDouble(discount)) {
          base -= discount;
        }

        return getVat(row, base);
      }
    }
  }

  @Override
  public Integer initExport(XSheet sheet) {
    if (sheet == null) {
      return null;

    } else {
      XStyle style = XStyle.right();
      style.setFormat(Format.getDecimalPattern(2));
      style.setFontRef(sheet.registerFont(XFont.bold()));

      return sheet.registerStyle(style);
    }
  }

  @Override
  public String render(IsRow row) {
    Double v = evaluate(row);
    return (v == null) ? null : BeeUtils.toString(v, 2);
  }

  protected Double evaluate(IsRow row) {
    return getTotal(row);
  }

  private Double getAmount(IsRow row) {
    if (row == null) {
      return null;

    } else if (data.containsKey(COL_TRADE_AMOUNT)) {
      return row.getDouble(data.get(COL_TRADE_AMOUNT));

    } else if (data.containsKey(COL_TRADE_ITEM_PRICE)) {
      Double price = row.getDouble(data.get(COL_TRADE_ITEM_PRICE));

      if (data.containsKey(COL_TRADE_ITEM_QUANTITY) && price != null) {
        Double qty = row.getDouble(data.get(COL_TRADE_ITEM_QUANTITY));
        return (qty == null) ? null : (price * qty);
      } else {
        return price;
      }

    } else {
      return null;
    }
  }

  private Double getDiscount(IsRow row, Double base) {
    if (base != null && data.containsKey(COL_TRADE_DISCOUNT)) {
      return BeeUtils.percent(base, row.getDouble(data.get(COL_TRADE_DISCOUNT)));
    } else {
      return null;
    }
  }

  private Double getVat(IsRow row, Double base) {
    if (base != null && data.containsKey(COL_TRADE_VAT)) {
      Double vat = row.getDouble(data.get(COL_TRADE_VAT));

      if (vat != null && data.containsKey(COL_TRADE_VAT_PERC)
          && BeeUtils.isTrue(row.getBoolean(data.get(COL_TRADE_VAT_PERC)))) {

        if (data.containsKey(COL_TRADE_VAT_PLUS)
            && !BeeUtils.isTrue(row.getBoolean(data.get(COL_TRADE_VAT_PLUS)))) {
          return BeeUtils.percentInclusive(base, vat);
        } else {
          return BeeUtils.percent(base, vat);
        }

      } else {
        return vat;
      }

    } else {
      return null;
    }
  }
}
