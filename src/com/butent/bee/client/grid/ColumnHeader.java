package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;

import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.shared.data.IsColumn;

public class ColumnHeader extends Header<String> {
  private final IsColumn dataColumn;

  public ColumnHeader(IsColumn dataColumn) {
    super(new HeaderCell());
    this.dataColumn = dataColumn;
  }

  @Override
  public String getValue() {
    return dataColumn.getLabel();
  }

  @Override
  public void render(Context context, SafeHtmlBuilder sb) {
    if (context instanceof ColumnContext) {
      renderHeader((ColumnContext) context, sb);
    } else {
      super.render(context, sb);
    }
  }

  public void renderHeader(ColumnContext context, SafeHtmlBuilder sb) {
    String label = getValue();
    CellGrid grid = context.getGrid();
    
    if (grid != null) {
      int sortIndex = grid.getSortIndex(label);
      if (sortIndex >= 0) {
        sb.append(sortIndex);
        sb.append(grid.isSortAscending(label) ? '\u21e7' : '\u21e9');
      }
    }

    if (label != null) {
      sb.appendEscaped(label);
    }   
  }
}
