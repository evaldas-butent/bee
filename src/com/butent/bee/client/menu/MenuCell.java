package com.butent.bee.client.menu;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.shared.menu.Menu;
import com.butent.bee.shared.menu.MenuItem;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Holds a representation of a menu item in a cell format and lets the system to catch it's click
 * event.
 */

public class MenuCell extends AbstractCell<Menu> {
  private static String[] events = new String[] {"mousedown"};

  public MenuCell() {
    super(events);
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, Menu value, NativeEvent event,
      ValueUpdater<Menu> valueUpdater) {
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
    if (value != null && (value instanceof MenuItem) && isSelectionEvent(event.getType())) {
      new MenuCommand(((MenuItem) value).getService(),
          ((MenuItem) value).getParameters()).execute();
    }
  }

  @Override
  public void render(Context context, Menu value, SafeHtmlBuilder sb) {
    if (value != null) {
      sb.appendEscaped(value.getLabel());
    }
  }

  private boolean isSelectionEvent(String type) {
    boolean ok = false;

    for (String event : events) {
      if (BeeUtils.same(event, type)) {
        ok = true;
        break;
      }
    }
    return ok;
  }
}
