package com.butent.bee.client.grid.cell;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.client.grid.CellContext;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Manages rendering of cells for calculated columns.
 */

public class CalculatedCell extends AbstractCell<String> {

  public CalculatedCell() {
    super();
  }

  @Override
  public void render(CellContext context, String value, SafeHtmlBuilder sb) {
    if (!BeeUtils.isEmpty(value)) {
      sb.appendEscaped(value);
    }
  }
}
