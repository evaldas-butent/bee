package com.butent.bee.client.grid.cell;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;

import com.butent.bee.client.grid.CellContext;
import com.butent.bee.client.i18n.HasNumberFormat;

/**
 * Manages rendering and format of a number type cell.
 */

public class NumberCell<C extends Number> extends AbstractCell<C> implements HasNumberFormat {

  private static SafeHtmlRenderer<String> renderer = SimpleSafeHtmlRenderer.getInstance();

  private NumberFormat format;

  public NumberCell(NumberFormat format) {
    this.format = format;
  }

  @Override
  public NumberFormat getNumberFormat() {
    return format;
  }

  @Override
  public void onBrowserEvent(CellContext context, Element parent, C value, NativeEvent event) {
  }

  @Override
  public void render(CellContext context, C value, SafeHtmlBuilder sb) {
    if (value != null) {
      sb.append(renderer.render((format == null) ? value.toString() : format.format(value)));
    }
  }

  @Override
  public void setNumberFormat(NumberFormat numberFormat) {
    this.format = numberFormat;
  }
}
