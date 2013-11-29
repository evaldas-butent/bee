package com.butent.bee.client.modules.trade;

import com.google.common.collect.Maps;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.ProvidesGridColumnRenderer;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

public class TotalRenderer extends AbstractCellRenderer {

  public static class Provider implements ProvidesGridColumnRenderer {
    @Override
    public AbstractCellRenderer getRenderer(String columnName,
        List<? extends IsColumn> dataColumns, ColumnDescription columnDescription) {

      return new TotalRenderer(dataColumns);
    }
  }

  final Map<String, Integer> data = Maps.newLinkedHashMap();

  public TotalRenderer(List<? extends IsColumn> columns) {
    super(null);

    for (String col : new String[] {COL_TRADE_AMOUNT, COL_TRADE_ITEM_QUANTITY,
        COL_TRADE_ITEM_PRICE, COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC}) {

      int idx = DataUtils.getColumnIndex(col, columns);

      if (idx != BeeConst.UNDEF) {
        data.put(col, idx);
      }
    }
  }

  @Override
  public String render(IsRow row) {
    double total = 0.0;

    if (data.containsKey(COL_TRADE_AMOUNT)) {
      total = BeeUtils.unbox(row.getDouble(data.get(COL_TRADE_AMOUNT)));

    } else if (data.containsKey(COL_TRADE_ITEM_PRICE)) {
      total = BeeUtils.unbox(row.getDouble(data.get(COL_TRADE_ITEM_PRICE)));

      if (data.containsKey(COL_TRADE_ITEM_QUANTITY)) {
        total *= BeeUtils.unbox(row.getDouble(data.get(COL_TRADE_ITEM_QUANTITY)));
      }
    }
    if (data.containsKey(COL_TRADE_VAT_PLUS)
        && BeeUtils.unbox(row.getBoolean(data.get(COL_TRADE_VAT_PLUS)))) {

      if (data.containsKey(COL_TRADE_VAT)) {
        double vat = BeeUtils.unbox(row.getDouble(data.get(COL_TRADE_VAT)));

        if (data.containsKey(COL_TRADE_VAT_PERC)
            && BeeUtils.unbox(row.getBoolean(data.get(COL_TRADE_VAT_PERC)))) {
          vat = total / 100 * vat;
        }
        total += vat;
      }
    }
    return BeeUtils.toString(total, 2);
  }
}
