package com.butent.bee.client.grid;

import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.shared.data.IsRow;

/**
 * Is an abstract class for cell view data presentation components, contains mandatory methods for
 * setting cell view's attributes.
 */

public abstract class AbstractCellView {
  private int cellIndex = 0;
  private int rowIndex = 0;
  private HasTableDefinition source;

  public AbstractCellView(HasTableDefinition sourceTableDef) {
    this.source = sourceTableDef;
  }

  public int getCellIndex() {
    return cellIndex;
  }

  public int getRowIndex() {
    return rowIndex;
  }

  public HasTableDefinition getSourceTableDefinition() {
    return source;
  }

  public abstract void setHorizontalAlignment(HorizontalAlignmentConstant align);

  public abstract void setHTML(String html);

  public abstract void setText(String text);

  public abstract void setVerticalAlignment(VerticalAlignmentConstant align);

  public abstract void setWidget(Widget widget);

  protected void renderCellImpl(int rowIdx, int cellIdx, IsRow rowValue,
      ColumnDefinition columnDef) {
    this.rowIndex = rowIdx;
    this.cellIndex = cellIdx;
    renderRowValue(rowValue, columnDef);
  }

  protected void renderRowValue(IsRow rowValue, ColumnDefinition columnDef) {
    columnDef.getCellRenderer().renderRowValue(rowValue, columnDef, this);
  }
}
