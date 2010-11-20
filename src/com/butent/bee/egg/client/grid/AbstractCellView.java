package com.butent.bee.egg.client.grid;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

public abstract class AbstractCellView<RowType> {
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

  protected void renderCellImpl(int rowIdx, int cellIdx,
      RowType rowValue, ColumnDefinition<RowType, ?> columnDef) {
    this.rowIndex = rowIdx;
    this.cellIndex = cellIdx;
    renderRowValue(rowValue, columnDef);
  }

  protected <ColType> void renderRowValue(RowType rowValue, ColumnDefinition<RowType, ColType> columnDef) {
    columnDef.getCellRenderer().renderRowValue(rowValue, columnDef, this);
  }
}
