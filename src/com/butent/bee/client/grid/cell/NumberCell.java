package com.butent.bee.client.grid.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;

import com.butent.bee.client.i18n.HasNumberFormat;

/**
 * Manages rendering and format of a number type cell.
 */

public class NumberCell<C extends Number> extends AbstractCell<C> implements HasNumberFormat {

  public static SafeHtmlRenderer<String> renderer = SimpleSafeHtmlRenderer.getInstance();

  private NumberFormat format;

  public NumberCell(NumberFormat format) {
    this.format = format;
  }

  public NumberFormat getNumberFormat() {
    return format;
  }

  @Override
  public void render(Context context, C value, SafeHtmlBuilder sb) {
    if (value != null) {
      sb.append(renderer.render((format == null) ? value.toString() : format.format(value)));
    }
  }

  public void setNumberFormat(NumberFormat format) {
    this.format = format;
  }
}
