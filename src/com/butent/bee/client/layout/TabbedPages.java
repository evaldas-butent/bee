package com.butent.bee.client.layout;

import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

/**
 * Enables to use a panel with tab pages.
 */

public class TabbedPages extends TabLayoutPanel implements HasId {

  private Map<String, BeeCommand> pageCommands = Maps.newHashMap();

  private class PageHandler implements BeforeSelectionHandler<Integer> {
    public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
      String id = DomUtils.getId(getWidget(event.getItem()));
      if (BeeUtils.isEmpty(id)) {
        return;
      }
      BeeCommand onPage = pageCommands.get(id);
      if (onPage != null) {
        onPage.execute();
      }
    }
  }

  public TabbedPages(double barHeight, Unit barUnit) {
    super(barHeight, barUnit);
    DomUtils.createId(this, getIdPrefix());
  }

  public void add(Widget child, String text, BeeCommand onPage) {
    super.add(child, text);
    addCommand(child, onPage);
  }

  public void add(Widget child, Widget tab, BeeCommand onPage) {
    super.add(child, tab);
    addCommand(child, onPage);
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "tabbed";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void addCommand(Widget child, BeeCommand onPage) {
    if (onPage == null) {
      return;
    }
    String id = DomUtils.getId(child);
    Assert.notEmpty(id, "page widget has no id");

    if (pageCommands.isEmpty()) {
      addBeforeSelectionHandler(new PageHandler());
    }
    pageCommands.put(id, onPage);
  }
}
