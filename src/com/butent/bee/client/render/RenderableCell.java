package com.butent.bee.client.render;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.client.grid.CellContext;
import com.butent.bee.client.grid.cell.AbstractCell;
import com.butent.bee.shared.utils.BeeUtils;

public class RenderableCell extends AbstractCell<String> {

  public RenderableCell() {
    super();
  }

  @Override
  public void render(CellContext context, String value, SafeHtmlBuilder sb) {
    if (!BeeUtils.isEmpty(value)) {
      sb.appendEscaped(value);
    }
  }
}
