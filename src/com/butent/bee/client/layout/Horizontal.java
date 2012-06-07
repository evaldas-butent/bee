package com.butent.bee.client.layout;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Contains a panel that lays all of its widgets out in a single horizontal row.
 */

public class Horizontal extends CellVector {

  private final Element tableRow;

  public Horizontal() {
    super();

    tableRow = DOM.createTR();
    DOM.appendChild(getBody(), tableRow);
  }

  @Override
  public void add(Widget w) {
    Element td = createAlignedTd();
    DOM.appendChild(tableRow, td);
    add(w, td);
  }
  
  public String getIdPrefix() {
    return "hor";
  }

  public void insert(Widget w, int beforeIndex) {
    checkIndexBoundsForInsertion(beforeIndex);

    Element td = createAlignedTd();
    DOM.insertChild(tableRow, td, beforeIndex);
    insert(w, td, beforeIndex, false);
  }

  @Override
  public boolean remove(Widget w) {
    Element td = DOM.getParent(w.getElement());
    boolean removed = super.remove(w);
    if (removed) {
      DOM.removeChild(tableRow, td);
    }
    return removed;
  }
}
