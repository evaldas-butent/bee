package com.butent.bee.client.grid;

import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;

import com.butent.bee.client.grid.cell.AbstractCell;
import com.butent.bee.shared.EventState;

public abstract class Header<H> {

  private final AbstractCell<H> cell;

  public Header(AbstractCell<H> cell) {
    this.cell = cell;
  }

  public AbstractCell<H> getCell() {
    return cell;
  }

  public abstract H getValue();

  public EventState onBrowserEvent(CellContext context, Element elem, Event event) {
    return cell.onBrowserEvent(context, elem, getValue(), event);
  }

  public void render(CellContext context, SafeHtmlBuilder sb) {
    cell.render(context, getValue(), sb);
  }
}
