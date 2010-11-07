package com.butent.bee.egg.client.pst;

public class DefaultRowRenderer<RowType> implements RowRenderer<RowType> {
  private String[] rowColors;

  public DefaultRowRenderer() {
    this(null);
  }

  public DefaultRowRenderer(String[] rowColors) {
    this.rowColors = rowColors;
  }

  public void renderRowValue(RowType rowValue, AbstractRowView<RowType> view) {
    if (rowColors != null) {
      int index = view.getRowIndex() % rowColors.length;
      view.setStyleAttribute("background", rowColors[index]);
    }
  }
}
