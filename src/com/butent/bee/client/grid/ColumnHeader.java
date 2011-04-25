package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.sort.SortInfo;

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
    String label = getValue();
    Object key = context.getKey();
    
    if (key instanceof SortInfo) {
      sb.append(((SortInfo) key).getIndex());
      sb.append(((SortInfo) key).isAscending() ? '\u2191' : '\u2193');
    }
    
    if (label != null) {
      sb.appendEscaped(label);
    }   
  }
}
