package com.butent.bee.client.modules.finance.analysis;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.RenderingEvent;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.finance.Dimensions;
import com.butent.bee.shared.modules.finance.analysis.AnalysisSplit;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.EditorDescription;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class AnalysisRowsGrid extends AbstractGridInterceptor {

  public AnalysisRowsGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new AnalysisRowsGrid();
  }

  @Override
  public void beforeRender(GridView gridView, RenderingEvent event) {
    IsRow parentRow = ViewHelper.getFormRow(gridView);

    if (parentRow != null) {
      boolean changed = false;
      CellGrid grid = getGridView().getGrid();

      Long indicator = Data.getLong(VIEW_ANALYSIS_HEADERS, parentRow,
          COL_ANALYSIS_HEADER_INDICATOR);
      changed |= grid.setColumnVisible(COL_ANALYSIS_ROW_INDICATOR, !DataUtils.isId(indicator));

      Long type = Data.getLong(VIEW_ANALYSIS_HEADERS, parentRow, COL_ANALYSIS_HEADER_BUDGET_TYPE);
      changed |= grid.setColumnVisible(COL_ANALYSIS_ROW_BUDGET_TYPE, !DataUtils.isId(type));

      for (int dimension = 1; dimension <= Dimensions.getObserved(); dimension++) {
        Boolean visible = Data.getBoolean(VIEW_ANALYSIS_HEADERS, parentRow,
            colAnalysisShowRowDimension(dimension));

        changed |= grid.setColumnVisible(Dimensions.getRelationColumn(dimension),
            BeeUtils.isTrue(visible));
      }

      Boolean showEmployee = Data.getBoolean(VIEW_ANALYSIS_HEADERS, parentRow,
          COL_ANALYSIS_SHOW_ROW_EMPLOYEE);
      changed |= grid.setColumnVisible(COL_ANALYSIS_ROW_EMPLOYEE, BeeUtils.isTrue(showEmployee));

      int splitLevels = BeeUtils.unbox(Data.getInteger(VIEW_ANALYSIS_HEADERS, parentRow,
          COL_ANALYSIS_ROW_SPLIT_LEVELS));

      for (int i = 0; i < COL_ANALYSIS_ROW_SPLIT.length; i++) {
        changed |= grid.setColumnVisible(COL_ANALYSIS_ROW_SPLIT[i], i < splitLevels);
      }
      changed |= grid.setColumnVisible(COL_ANALYSIS_ROW_TOTAL, splitLevels > 0);

      if (changed) {
        event.setDataChanged();
      }
    }

    super.beforeRender(gridView, event);
  }

  @Override
  public AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription, CellSource cellSource) {

    if (ArrayUtils.contains(COL_ANALYSIS_ROW_SPLIT, columnName)) {
      return new SplitRenderer(cellSource);
    } else {
      return super.getRenderer(columnName, dataColumns, columnDescription, cellSource);
    }
  }

  @Override
  public Editor maybeCreateEditor(String source, EditorDescription editorDescription,
      boolean embedded) {

    if (ArrayUtils.contains(COL_ANALYSIS_ROW_SPLIT, source)) {
      ListBox listBox = new ListBox();

      for (AnalysisSplit split : AnalysisSplit.values()) {
        if (split.visibleForRows()) {
          listBox.addItem(split.getCaption(), split.name().toLowerCase());
        }
      }

      return listBox;

    } else {
      return super.maybeCreateEditor(source, editorDescription, embedded);
    }
  }
}
