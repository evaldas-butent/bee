package com.butent.bee.egg.client.pst;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

import java.util.Iterator;
import java.util.List;

public interface TableDefinition<RowType> {

  public abstract static class AbstractCellView<RowType> {
    private int cellIndex = 0;
    private int rowIndex = 0;
    private HasTableDefinition<RowType> source;

    public AbstractCellView(HasTableDefinition<RowType> sourceTableDef) {
      this.source = sourceTableDef;
    }

    public int getCellIndex() {
      return cellIndex;
    }

    public int getRowIndex() {
      return rowIndex;
    }

    public HasTableDefinition<RowType> getSourceTableDefinition() {
      return source;
    }

    public abstract void setHorizontalAlignment(HorizontalAlignmentConstant align);

    public abstract void setHTML(String html);

    public abstract void setStyleAttribute(String attr, String value);

    public abstract void setStyleName(String stylename);

    public abstract void setText(String text);

    public abstract void setVerticalAlignment(VerticalAlignmentConstant align);

    public abstract void setWidget(Widget widget);

    protected void renderCellImpl(int rowIndex, int cellIndex,
        RowType rowValue, ColumnDefinition<RowType, ?> columnDef) {
      this.rowIndex = rowIndex;
      this.cellIndex = cellIndex;
      renderRowValue(rowValue, columnDef);
    }

    protected <ColType> void renderRowValue(RowType rowValue, ColumnDefinition<RowType, ColType> columnDef) {
      columnDef.getCellRenderer().renderRowValue(rowValue, columnDef, this);
    }
  }

  public abstract static class AbstractRowView<RowType> {
    private int rowIndex = 0;
    private AbstractCellView<RowType> cellView;

    public AbstractRowView(AbstractCellView<RowType> cellView) {
      this.cellView = cellView;
    }

    public int getRowIndex() {
      return rowIndex;
    }

    public HasTableDefinition<RowType> getSourceTableDefinition() {
      return cellView.getSourceTableDefinition();
    }

    public abstract void setStyleAttribute(String attr, String value);

    public abstract void setStyleName(String stylename);

    protected void renderRowImpl(int rowIndex, RowType rowValue,
        RowRenderer<RowType> rowRenderer,
        List<ColumnDefinition<RowType, ?>> visibleColumns) {
      this.rowIndex = rowIndex;
      renderRowValue(rowValue, rowRenderer);
      int numColumns = visibleColumns.size();
      for (int i = 0; i < numColumns; i++) {
        cellView.renderCellImpl(rowIndex, i, rowValue, visibleColumns.get(i));
      }
    }

    protected void renderRowsImpl(int startRowIndex,
        Iterator<RowType> rowValues, RowRenderer<RowType> rowRenderer,
        List<ColumnDefinition<RowType, ?>> visibleColumns) {
      int curRow = startRowIndex;
      while (rowValues.hasNext()) {
        renderRowImpl(curRow, rowValues.next(), rowRenderer, visibleColumns);
        curRow++;
      }
    }

    protected void renderRowValue(RowType rowValue, RowRenderer<RowType> rowRenderer) {
      rowRenderer.renderRowValue(rowValue, this);
    }
  }

  RowRenderer<RowType> getRowRenderer();

  List<ColumnDefinition<RowType, ?>> getVisibleColumnDefinitions();

  void renderRows(int startRowIndex, Iterator<RowType> rowValues, AbstractRowView<RowType> view);
}
