package com.butent.bee.client.imports;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImportMappingsGrid extends AbstractGridInterceptor implements SelectorEvent.Handler {

  private final String viewName;
  private BeeRowSet cache;
  private boolean waiting;

  public ImportMappingsGrid(String viewName) {
    this.viewName = viewName;
  }

  @Override
  public void afterCreateEditor(String source, Editor editor, boolean embedded) {
    if (BeeUtils.same(source, AdministrationConstants.COL_IMPORT_MAPPING)
        && editor instanceof DataSelector) {

      ((DataSelector) editor).addSelectorHandler(this);
    }
    super.afterCreateEditor(source, editor, embedded);
  }

  @Override
  public ColumnDescription beforeCreateColumn(final GridView gridView, ColumnDescription descr) {
    if (BeeUtils.same(descr.getId(), AdministrationConstants.COL_IMPORT_MAPPING)) {
      Relation relation = Data.getRelation(viewName);

      if (relation == null) {
        List<String> columns = new ArrayList<>();

        for (BeeColumn column : Data.getColumns(viewName)) {
          columns.add(column.getId());
        }
        relation = Relation.create(viewName, columns);
        LogUtils.getRootLogger().warning("Missing relation info:", viewName);
      }
      Relation rel = descr.getRelation();

      if (Objects.isNull(rel) || !rel.isNewRowEnabled()) {
        relation.disableNewRow();
      }
      if (Objects.isNull(rel) || !rel.isEditEnabled(true)) {
        relation.disableEdit();
      }
      descr.setRelation(relation);

      Queries.getRowSet(viewName, relation.getOriginalRenderColumns(),
          new Queries.RowSetCallback() {
            @Override
            public void onSuccess(BeeRowSet result) {
              cache = result;

              if (waiting) {
                gridView.refresh(true, true);
              }
            }
          });
    }
    return super.beforeCreateColumn(gridView, descr);
  }

  @Override
  public GridInterceptor getInstance() {
    return null;
  }

  @Override
  public AbstractCellRenderer getRenderer(String columnName,
      List<? extends IsColumn> dataColumns, ColumnDescription columnDescription,
      CellSource cellSource) {

    if (BeeUtils.same(columnName, AdministrationConstants.COL_IMPORT_MAPPING)) {
      return new AbstractCellRenderer(cellSource) {
        @Override
        public String render(IsRow row) {
          if (cache != null) {
            Long mapping = getLong(row);

            if (DataUtils.isId(mapping)) {
              BeeRow data = cache.getRowById(mapping);

              if (data != null) {
                return BeeUtils.joinWords(data.getValues());
              }
            }
          } else {
            waiting = true;
          }
          return null;
        }
      };
    }
    return super.getRenderer(columnName, dataColumns, columnDescription, cellSource);
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (event.hasRelatedView(viewName) && event.isRowCreated()) {
      List<String> data = new ArrayList<>();
      BeeRow newRow = event.getNewRow();

      for (BeeColumn column : cache.getColumns()) {
        data.add(Data.getString(viewName, newRow, column.getId()));
      }
      cache.addRow(newRow.getId(), newRow.getVersion(), data);
    }
  }
}
