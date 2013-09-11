package com.butent.bee.client.grid.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import com.butent.bee.shared.utils.BeeUtils;

public class FooterCell extends AbstractCell<String> {

  public FooterCell() {
    super();
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    if (!BeeUtils.isEmpty(value)) {
      sb.append(SafeHtmlUtils.fromTrustedString(value));
    }
  }
}
