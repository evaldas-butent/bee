package com.butent.bee.egg.client.menu;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

import com.butent.bee.egg.shared.menu.MenuEntry;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class MenuCell extends AbstractCell<MenuEntry> {
  private static String[] events = new String[]{"mousedown"};

  public MenuCell() {
    super(events);
  }

  @Override
  public void onBrowserEvent(Element parent, MenuEntry value, Object key,
      NativeEvent event, ValueUpdater<MenuEntry> valueUpdater) {
    super.onBrowserEvent(parent, value, key, event, valueUpdater);

    if (value != null && !BeeUtils.isEmpty(value.getService())
        && isSelectionEvent(event.getType())) {
      new MenuCommand(value.getService(), value.getParameters()).execute();
    }
  }

  @Override
  public void render(MenuEntry value, Object key, StringBuilder sb) {
    if (value != null) {
      sb.append(value.getText());
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
