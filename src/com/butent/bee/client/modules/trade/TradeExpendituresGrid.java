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
import com.butent.bee.shared.data.RowToDouble;
import com.butent.bee.shared.data.value.DecimalValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.modules.trade.TradeVatMode;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.List;

public class TradeExpendituresGrid extends AbstractGridInterceptor {

  private final class SumRenderer extends AbstractCellRenderer implements HasRowValue {

    private final RowToDouble valueFunction;

    private SumRenderer(RowToDouble valueFunction) {
      super(null);
      this.valueFunction = valueFunction;
    }

    @Override
    public boolean dependsOnSource(String source) {
      return BeeUtils.inList(source, COL_EXPENDITURE_TYPE, COL_EXPENDITURE_AMOUNT,
          COL_EXPENDITURE_VAT, COL_EXPENDITURE_VAT_IS_PERCENT);
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
      if (row == null) {
        return BeeConst.DOUBLE_ZERO;
      } else {
        Double value = valueFunction.apply(row);
        return BeeUtils.isDouble(value) ? value : BeeConst.DOUBLE_ZERO;
      }
    }
  }

  TradeExpendituresGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeExpendituresGrid();
  }

  @Override
  public AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription, CellSource cellSource) {

    if (!BeeUtils.isEmpty(columnName)) {
      switch (columnName) {
        case "VatAmount":
          return new SumRenderer(this::getVatAmount);

        case "Total":
          return new SumRenderer(this::getTotal);
      }
    }

    return super.getRenderer(columnName, dataColumns, columnDescription, cellSource);
  }

  private Double getVatAmount(IsRow row) {
    TradeVatMode vatMode = getVatMode(row);
    if (vatMode == null) {
      return null;
    }

    Double amount = getAmount(row);
    if (!isValid(amount)) {
      return null;
    }

    Double vat = row.getDouble(getDataIndex(COL_EXPENDITURE_VAT));

    if (isValid(vat)) {
      if (row.isNull(getDataIndex(COL_EXPENDITURE_VAT_IS_PERCENT))) {
        return round(vat);
      } else {
        return round(vatMode.computePercent(amount, vat));
      }

    } else {
      return null;
    }
  }

  private Double getTotal(IsRow row) {
    Double amount = getAmount(row);
    if (!isValid(amount)) {
      return null;
    }

    TradeVatMode vatMode = getVatMode(row);
    if (vatMode == TradeVatMode.PLUS) {
      Double vat = getVatAmount(row);
      if (isValid(vat)) {
        return round(amount) + round(vat);
      }
    }

    return round(amount);
  }

  private Double getAmount(IsRow row) {
    return round(row.getDouble(getDataIndex(COL_EXPENDITURE_AMOUNT)));
  }

  private TradeVatMode getVatMode(IsRow row) {
    return EnumUtils.getEnumByIndex(TradeVatMode.class,
        row.getInteger(getDataIndex(COL_OPERATION_VAT_MODE)));
  }

  private static boolean isValid(Double x) {
    return BeeUtils.nonZero(x);
  }

  private static Double round(Double x) {
    return isValid(x) ? BeeUtils.round(x, 2) : null;
  }
}
