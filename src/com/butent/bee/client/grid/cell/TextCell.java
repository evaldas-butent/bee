package com.butent.bee.client.grid.cell;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import com.butent.bee.client.grid.CellContext;
import com.butent.bee.shared.utils.BeeUtils;

public class TextCell extends AbstractCell<String> {

  public TextCell() {
    super();
  }

  @Override
  public void onBrowserEvent(CellContext context, Element parent, String value, NativeEvent event) {
  }

  @Override
  public void render(CellContext context, String value, SafeHtmlBuilder sb) {
    if (!BeeUtils.isEmpty(value)) {
      sb.append(SafeHtmlUtils.fromString(value));
    }
  }
}
