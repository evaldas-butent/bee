package com.butent.bee.client.grid.cell;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.grid.CellContext;
import com.butent.bee.client.i18n.HasNumberFormat;

/**
 * Manages rendering and format of a number type cell.
 */

public class NumberCell<C extends Number> extends AbstractCell<C> implements HasNumberFormat {

  private NumberFormat format;

  public NumberCell(NumberFormat format) {
    this.format = format;
  }

  @Override
  public NumberFormat getNumberFormat() {
    return format;
  }

  @Override
  public String render(CellContext context, C value) {
    if (value != null) {
      return (format == null) ? value.toString() : format.format(value);
    } else {
      return null;
    }
  }

  @Override
  public void setNumberFormat(NumberFormat numberFormat) {
    this.format = numberFormat;
  }
}
