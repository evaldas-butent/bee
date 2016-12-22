package com.butent.bee.client.view.grid.interceptor;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class ParentRowRefreshGrid extends AbstractGridInterceptor {
  private boolean parentExists;

  @Override
  public boolean previewCellUpdate(CellUpdateEvent event) {
    return previewModify(Collections.singleton(event.getRowId()));
  }

  @Override
  public boolean previewMultiDelete(MultiDeleteEvent event) {
    return previewModify(event.getRowIds());
  }

  @Override
  public boolean previewRowDelete(RowDeleteEvent event) {
    return previewModify(Collections.singleton(event.getRowId()));
  }

  @Override
  public boolean previewRowInsert(RowInsertEvent event) {
    if (parentExists()) {
      getGridView().ensureRelId(relId -> {
        if (Objects.equals(relId, Data.getLong(event.getViewName(), event.getRow(),
            getGridView().getRelColumn()))) {
          previewModify(null);
        }
      });
    }
    return true;
  }

  @Override
  public boolean previewRowUpdate(RowUpdateEvent event) {
    return previewModify(Collections.singleton(event.getRowId()));
  }

  @Override
  public GridInterceptor getInstance() {
    return new ParentRowRefreshGrid();
  }

  public boolean previewModify(Set<Long> rowIds) {
    if (Objects.isNull(rowIds)
        || getGridView().getRowData().stream().anyMatch(row -> rowIds.contains(row.getId()))) {
      FormView parentForm = ViewHelper.getForm(getGridView());

      if (Objects.nonNull(parentForm) && DomUtils.isVisible(parentForm.getElement())
          && DataUtils.isId(parentForm.getActiveRowId())) {
        String view = parentForm.getViewName();
        Queries.getRow(view, parentForm.getActiveRowId(), RowCallback.refreshRow(view));
      }
      return true;
    }
    return false;
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    parentExists = DataUtils.isId(event.getRowId());
    super.onParentRow(event);
  }

  public boolean parentExists() {
    return parentExists;
  }
}
