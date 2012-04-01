package com.butent.bee.client.render;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.shared.utils.BeeUtils;

public class RenderableCell extends AbstractCell<String> {

  public RenderableCell() {
    super();
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    if (!BeeUtils.isEmpty(value)) {
      sb.appendEscaped(value);
    }
  }
}
