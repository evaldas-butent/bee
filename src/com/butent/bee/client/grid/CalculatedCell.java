package com.butent.bee.client.grid;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.shared.utils.BeeUtils;

public class CalculatedCell extends AbstractCell<String> {
  
  public CalculatedCell() {
    super();
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    if (!BeeUtils.isEmpty(value)) {
      sb.appendEscaped(value);
    }
  }
}
