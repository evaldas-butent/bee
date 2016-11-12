package com.butent.bee.client.modules.cars;

import static com.butent.bee.shared.modules.cars.CarsConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StageActionsGrid extends AbstractGridInterceptor {

  private String dataView;

  @Override
  public GridInterceptor getInstance() {
    return new StageActionsGrid();
  }

  @Override
  public AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription, CellSource cellSource) {

    if (BeeUtils.same(columnName, COL_STAGE_ACTION)) {
      return new AbstractCellRenderer(cellSource) {
        private int actionIdx = DataUtils.getColumnIndex(COL_STAGE_ACTION, dataColumns);

        @Override
        public String render(IsRow row) {
          String action = row.getString(actionIdx);
          return BeeUtils.notEmpty(StageUtils.getActions(dataView).get(action), action);
        }
      };
    }
    return super.getRenderer(columnName, dataColumns, columnDescription, cellSource);
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    dataView = null;
    IsRow parentRow = event.getRow();

    if (Objects.nonNull(parentRow)) {
      dataView = Data.getString(event.getViewName(), parentRow, COL_STAGE_VIEW);
    }
    super.onParentRow(event);
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    List<String> actions = new ArrayList<>();
    List<String> captions = new ArrayList<>();

    StageUtils.getActions(dataView).forEach((action, caption) -> {
      if (!gridView.getRowData().stream()
          .anyMatch(row -> Objects.equals(row.getString(getDataIndex(COL_STAGE_ACTION)), action))) {
        actions.add(action);
        captions.add(caption);
      }
    });
    if (actions.isEmpty()) {
      getGridView().notifyWarning(Localized.dictionary().noData());
    } else {
      Global.choice(Localized.dictionary().calListOfActions(), null, captions, idx ->
          getGridView().ensureRelId(stage -> Queries.insert(getViewName(),
              Data.getColumns(getViewName(), Arrays.asList(COL_STAGE, COL_STAGE_ACTION)),
              Arrays.asList(BeeUtils.toString(stage), actions.get(idx)), null,
              new RowInsertCallback(getViewName()) {
                @Override
                public void onSuccess(BeeRow result) {
                  getGridPresenter().refresh(false, false);
                  super.onSuccess(result);
                }
              })));
    }
    return false;
  }
}