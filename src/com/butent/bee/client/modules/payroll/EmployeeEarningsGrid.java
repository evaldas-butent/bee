package com.butent.bee.client.modules.payroll;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasRowValue;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.DecimalValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.modules.payroll.PayrollUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class EmployeeEarningsGrid extends AbstractGridInterceptor {

  private static final class TotalEarnings extends AbstractCellRenderer implements HasRowValue {

    private final int percentIndex;
    private final int bonus1Index;
    private final int bonus2Index;

    private TotalEarnings(List<? extends IsColumn> columns) {
      super(null);

      this.percentIndex = DataUtils.getColumnIndex(COL_EARNINGS_BONUS_PERCENT, columns, true);
      this.bonus1Index = DataUtils.getColumnIndex(COL_EARNINGS_BONUS_1, columns, true);
      this.bonus2Index = DataUtils.getColumnIndex(COL_EARNINGS_BONUS_2, columns, true);
    }

    @Override
    public boolean dependsOnSource(String source) {
      return BeeUtils.inListSame(source, PRP_EARNINGS_AMOUNT, COL_EARNINGS_BONUS_PERCENT,
          COL_EARNINGS_BONUS_1, COL_EARNINGS_BONUS_2);
    }

    @Override
    public Value getRowValue(IsRow row) {
      Double v = total(row);
      return (v == null) ? null : DecimalValue.of(v);
    }

    @Override
    public ValueType getValueType() {
      return ValueType.DECIMAL;
    }

    @Override
    public String render(IsRow row) {
      Double v = total(row);
      return (v == null) ? null : BeeUtils.toString(v, 2);
    }

    private Double total(IsRow row) {
      if (row == null) {
        return null;
      } else {
        return PayrollUtils.calculateEarnings(row.getPropertyDouble(PRP_EARNINGS_AMOUNT),
            row.getDouble(percentIndex), row.getDouble(bonus1Index), row.getDouble(bonus2Index));
      }
    }
  }

  EmployeeEarningsGrid() {
  }

  @Override
  public BeeRowSet getInitialRowSet(GridDescription gridDescription) {
    return Data.createRowSet(gridDescription.getViewName());
  }

  @Override
  public GridInterceptor getInstance() {
    return new EmployeeEarningsGrid();
  }

  @Override
  public AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription, CellSource cellSource) {

    if ("Total".equals(columnName)) {
      return new TotalEarnings(dataColumns);
    } else {
      return super.getRenderer(columnName, dataColumns, columnDescription, cellSource);
    }
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    Filter filter = null;

    if (event.getRow() != null) {
      Long object = Data.getLong(event.getViewName(), event.getRow(), COL_PAYROLL_OBJECT);
      Integer year = Data.getInteger(event.getViewName(), event.getRow(), COL_EARNINGS_YEAR);
      Integer month = Data.getInteger(event.getViewName(), event.getRow(), COL_EARNINGS_MONTH);

      if (DataUtils.isId(object) && TimeUtils.isYear(year) && TimeUtils.isMonth(month)) {
        filter = Filter.and(Filter.equals(COL_PAYROLL_OBJECT, object),
            Filter.equals(COL_EARNINGS_YEAR, year),
            Filter.equals(COL_EARNINGS_MONTH, month));
      }
    }

    if (filter == null) {
      filter = Filter.isFalse();
    }

    if (getGridPresenter() != null) {
      getGridPresenter().getDataProvider().setDefaultParentFilter(filter);
      getGridPresenter().refresh(false, true);
    }
  }
}
