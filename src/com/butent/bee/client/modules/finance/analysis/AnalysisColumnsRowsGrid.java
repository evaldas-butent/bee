package com.butent.bee.client.modules.finance.analysis;

import com.butent.bee.client.data.Queries;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.cell.HeaderCell;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.finance.analysis.AnalysisSplit;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.EditorDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

abstract class AnalysisColumnsRowsGrid extends AbstractGridInterceptor {

  protected AnalysisColumnsRowsGrid() {
  }

  protected abstract String getSelectionColumnName();

  protected abstract boolean isSplitColumn(String columnName);

  protected abstract boolean isSplitVisible(AnalysisSplit analysisSplit);

  @Override
  public boolean afterCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      EditableColumn editableColumn) {

    if (BeeUtils.same(getSelectionColumnName(), columnName) && header != null) {
      header.getCell().addClickHandler(event -> {
        if (event.getSource() instanceof HeaderCell) {
          HeaderCell headerCell = (HeaderCell) event.getSource();

          if (headerCell.isCaptionEvent(event.getNativeEvent())) {
            headerCell.setEventCanceled(true);
            onSelectAll();
          }
        }
      });
    }

    return super.afterCreateColumn(columnName, dataColumns, column, header, footer, editableColumn);
  }

  @Override
  public AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription, CellSource cellSource) {

    if (isSplitColumn(columnName)) {
      return new SplitRenderer(cellSource);
    } else {
      return super.getRenderer(columnName, dataColumns, columnDescription, cellSource);
    }
  }

  @Override
  public Editor maybeCreateEditor(String source, EditorDescription editorDescription,
      boolean embedded) {

    if (isSplitColumn(source)) {
      ListBox listBox = new ListBox();

      for (AnalysisSplit split : AnalysisSplit.values()) {
        if (isSplitVisible(split)) {
          listBox.addItem(split.getCaption(), split.name().toLowerCase());
        }
      }

      return listBox;

    } else {
      return super.maybeCreateEditor(source, editorDescription, embedded);
    }
  }

  private void onSelectAll() {
    List<? extends IsRow> rows = getGridView().getRowData();
    int index = getDataIndex(getSelectionColumnName());

    if (!BeeUtils.isEmpty(rows) && index >= 0) {
      boolean selected = true;

      for (IsRow row : rows) {
        if (!row.isTrue(index)) {
          selected = false;
          break;
        }
      }

      selected = !selected;
      String newValue = selected ? Codec.pack(selected) : null;

      for (IsRow row : rows) {
        if (row.isTrue(index) != selected) {
          Queries.updateCellAndFire(getViewName(), row.getId(), row.getVersion(),
              getSelectionColumnName(), row.getString(index), newValue);
        }
      }
    }
  }
}
