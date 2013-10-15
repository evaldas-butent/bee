package com.butent.bee.client.grid;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.client.grid.cell.AbstractCell;

public abstract class Header<H> {

  private final AbstractCell<H> cell;

  public Header(AbstractCell<H> cell) {
    this.cell = cell;
  }

  public AbstractCell<H> getCell() {
    return cell;
  }

  public abstract H getValue();

  public void onBrowserEvent(CellContext context, Element elem, NativeEvent event) {
    cell.onBrowserEvent(context, elem, getValue(), event);
  }

  public void render(CellContext context, SafeHtmlBuilder sb) {
    cell.render(context, getValue(), sb);
  }
}
