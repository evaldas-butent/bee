package com.butent.bee.client.menu;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.shared.menu.MenuEntry;
import com.butent.bee.shared.utils.BeeUtils;

public class MenuCell extends AbstractCell<MenuEntry> {
  private static String[] events = new String[]{"mousedown"};

  public MenuCell() {
    super(events);
  }
  
  @Override
  public void onBrowserEvent(Context context, Element parent, MenuEntry value, NativeEvent event,
      ValueUpdater<MenuEntry> valueUpdater) {
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
    if (value != null && !BeeUtils.isEmpty(value.getService())
        && isSelectionEvent(event.getType())) {
      new MenuCommand(value.getService(), value.getParameters()).execute();
    }
  }

  @Override
  public void render(Context context, MenuEntry value, SafeHtmlBuilder sb) {
    if (value != null) {
      sb.appendEscaped(value.getText());
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
