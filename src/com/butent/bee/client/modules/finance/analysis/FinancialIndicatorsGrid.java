package com.butent.bee.client.modules.finance.analysis;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.finance.analysis.IndicatorKind;

public class FinancialIndicatorsGrid extends AbstractGridInterceptor {

  private final IndicatorKind kind;

  public FinancialIndicatorsGrid(IndicatorKind kind) {
    this.kind = kind;
  }

  @Override
  public GridInterceptor getInstance() {
    return new FinancialIndicatorsGrid(kind);
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    newRow.setValue(getDataIndex(COL_FIN_INDICATOR_KIND), kind.ordinal());
    return super.onStartNewRow(gridView, oldRow, newRow);
  }
}
