package com.butent.bee.client.grid.cell;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import com.butent.bee.client.grid.CellContext;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Manages rendering of cells containing arbitrary html.
 */

public class HtmlCell extends AbstractCell<String> {

  public HtmlCell() {
    super();
  }

  @Override
  public void render(CellContext context, String value, SafeHtmlBuilder sb) {
    if (!BeeUtils.isEmpty(value)) {
      sb.append(SafeHtmlUtils.fromTrustedString(value));
    }
  }
}
