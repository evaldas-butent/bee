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

public class StageTriggersGrid extends AbstractGridInterceptor {

  private String dataView;

  @Override
  public GridInterceptor getInstance() {
    return new StageTriggersGrid();
  }

  @Override
  public AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription, CellSource cellSource) {

    if (BeeUtils.same(columnName, COL_STAGE_TRIGGER)) {
      return new AbstractCellRenderer(cellSource) {
        private int triggerIdx = DataUtils.getColumnIndex(COL_STAGE_TRIGGER, dataColumns);

        @Override
        public String render(IsRow row) {
          String trigger = row.getString(triggerIdx);
          return BeeUtils.notEmpty(StageUtils.getTriggers(dataView).get(trigger), trigger);
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
    List<String> triggers = new ArrayList<>();
    List<String> captions = new ArrayList<>();

    StageUtils.getTriggers(dataView).forEach((trigger, caption) -> {
      if (!gridView.getRowData().stream().anyMatch(row ->
          Objects.equals(row.getString(getDataIndex(COL_STAGE_TRIGGER)), trigger))) {
        triggers.add(trigger);
        captions.add(caption);
      }
    });
    if (triggers.isEmpty()) {
      getGridView().notifyWarning(Localized.dictionary().noData());
    } else {
      Global.choice(Localized.dictionary().triggers(), null, captions, idx ->
          getGridView().ensureRelId(stage -> Queries.insert(getViewName(),
              Data.getColumns(getViewName(), Arrays.asList(COL_STAGE, COL_STAGE_TRIGGER)),
              Arrays.asList(BeeUtils.toString(stage), triggers.get(idx)), null,
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