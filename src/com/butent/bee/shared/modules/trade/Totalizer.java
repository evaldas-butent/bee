package com.butent.bee.shared.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasRowValue;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.DecimalValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Totalizer implements HasRowValue {

  private static final String[] COL_NAMES = new String[] {
      COL_TRADE_AMOUNT, COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE,
      COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC,
      COL_TRADE_DISCOUNT};

  private final Map<String, Integer> data = new HashMap<>();

  public Totalizer(List<? extends IsColumn> columns) {
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

  public Double getDiscount(IsRow row) {
    if (row == null) {
      return null;
    } else {
      return getDiscount(row, getAmount(row));
    }
  }

  @Override
  public Value getRowValue(IsRow row) {
    Double v = getTotal(row);
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
