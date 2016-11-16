package com.butent.bee.client.modules.finance.analysis;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.RenderingEvent;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.finance.Dimensions;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class BudgetEntriesGrid extends AbstractGridInterceptor {

  public BudgetEntriesGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new BudgetEntriesGrid();
  }

  @Override
  public void beforeRender(GridView gridView, RenderingEvent event) {
    IsRow parentRow = ViewHelper.getFormRow(gridView);

    if (parentRow != null) {
      boolean changed = false;

      Long indicator = Data.getLong(VIEW_BUDGET_HEADERS, parentRow, COL_BUDGET_HEADER_INDICATOR);
      changed |= getGridView().getGrid().setColumnVisible(COL_BUDGET_ENTRY_INDICATOR,
          !DataUtils.isId(indicator));

      Long type = Data.getLong(VIEW_BUDGET_HEADERS, parentRow, COL_BUDGET_HEADER_TYPE);
      changed |= getGridView().getGrid().setColumnVisible(COL_BUDGET_ENTRY_TYPE,
          !DataUtils.isId(type));

      Integer year = Data.getInteger(VIEW_BUDGET_HEADERS, parentRow, COL_BUDGET_HEADER_YEAR);
      changed |= getGridView().getGrid().setColumnVisible(COL_BUDGET_ENTRY_YEAR,
          !TimeUtils.isYear(year));

      for (int dimension = 1; dimension <= Dimensions.getObserved(); dimension++) {
        Boolean visible = Data.getBoolean(VIEW_BUDGET_HEADERS, parentRow,
            colBudgetShowEntryDimension(dimension));

        changed |= getGridView().getGrid().setColumnVisible(
            Dimensions.getRelationColumn(dimension), BeeUtils.isTrue(visible));
      }

      Boolean showEmployee = Data.getBoolean(VIEW_BUDGET_HEADERS, parentRow,
          COL_BUDGET_SHOW_ENTRY_EMPLOYEE);
      changed |= getGridView().getGrid().setColumnVisible(COL_BUDGET_ENTRY_EMPLOYEE,
          BeeUtils.isTrue(showEmployee));

      if (changed) {
        event.setDataChanged();
      }
    }

    super.beforeRender(gridView, event);
  }
}
