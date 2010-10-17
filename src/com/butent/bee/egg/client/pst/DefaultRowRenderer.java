package com.butent.bee.egg.client.pst;

import com.butent.bee.egg.client.pst.TableDefinition.AbstractRowView;

/**
 * The default {@link RowRenderer} used by the {@link DefaultTableDefinition}
 * when the user does not specify one.
 * 
 * @param <RowType> the type of the row value
 */
public class DefaultRowRenderer<RowType> implements RowRenderer<RowType> {
  /**
   * The alternating colors to apply to the rows.
   */
  private String[] rowColors;

  /**
   * Construct a new {@link DefaultRowRenderer}.
   */
  public DefaultRowRenderer() {
    this(null);
  }

  /**
   * Construct a new {@link DefaultRowRenderer}.
   * 
   * @param rowColors an array of alternating colors to apply to the rows
   */
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
