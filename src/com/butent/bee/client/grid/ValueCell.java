package com.butent.bee.client.grid;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.shared.data.value.Value;

public class ValueCell<C extends Value> extends AbstractCell<C> {

  @Override
  public void render(Context context, C value, SafeHtmlBuilder sb) {
    if (value != null) {
      sb.appendEscaped(value.getString());
    }
  }
}
