package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;

public class CargoExpensesGrid extends TransportVatGridInterceptor {

  private Long parentId;

  @Override
  public void afterCreateEditor(String source, Editor editor, boolean embedded) {
    if (BeeUtils.same(source, COL_CARGO_INCOME) && editor instanceof DataSelector) {
      ((DataSelector) editor).addSelectorHandler(event -> {
        if (BeeUtils.same(event.getRelatedViewName(), TBL_CARGO_INCOMES) && event.isOpened()) {
          if (DataUtils.isId(parentId)) {
            getGridView().ensureRelId(relId ->
                event.getSelector().setAdditionalFilter(Filter.equals(COL_CARGO, relId)));
          } else {
            event.getSelector().setAdditionalFilter(Filter.isFalse());
          }
        }
      });
    }
    super.afterCreateEditor(source, editor, embedded);
  }

  @Override
  public ColumnDescription beforeCreateColumn(GridView gridView, ColumnDescription descr) {
    if (!TransportHandler.bindExpensesToIncomes()
        && Objects.equals(descr.getId(), COL_CARGO_INCOME)) {
      return null;
    }
    return super.beforeCreateColumn(gridView, descr);
  }

  @Override
  public GridInterceptor getInstance() {
    return new CargoExpensesGrid();
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    parentId = event.getRowId();
    super.onParentRow(event);
  }
}
