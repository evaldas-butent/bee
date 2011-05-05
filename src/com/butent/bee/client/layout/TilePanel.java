package com.butent.bee.client.layout;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.tree.BeeTreeItem;
import com.butent.bee.client.utils.JreEmulation;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages a panel consisting of sever child tiles.
 */

public class TilePanel extends Split {
  public TilePanel() {
    super("bee-tile", 5);
    sinkEvents(Event.ONMOUSEDOWN);
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "tilepanel");
  }

  public Widget getChildById(String id) {
    Widget child = null;
    if (BeeUtils.isEmpty(id)) {
      return child;
    }

    for (Widget w : getChildren()) {
      if (w instanceof HasId) {
        if (BeeUtils.same(((HasId) w).getId(), id)) {
          child = w;
          break;
        }
      } else if (BeeUtils.same(DomUtils.getId(child), id)) {
        child = w;
        break;
      }
    }
    return child;
  }

  public List<TilePanel> getPanels() {
    List<TilePanel> lst = new ArrayList<TilePanel>();

    for (Widget w : getChildren()) {
      if (w instanceof TilePanel) {
        lst.add((TilePanel) w);
      }
    }
    return lst;
  }

  public int getTileIndex(Widget w) {
    int n = getWidgetIndex(w);
    if (n <= 0) {
      return n;
    }

    int idx = 0;
    for (int i = 0; i < n; i++) {
      if (!isSplitter(getChildren().get(i))) {
        idx++;
      }
    }
    return idx;
  }

  public BeeTreeItem getTree(String prefix, boolean splitters) {
    BeeTreeItem root = new BeeTreeItem(BeeUtils.concat(1, prefix, getId()));

    for (Widget child : getChildren()) {
      if (!splitters && isSplitter(child)) {
        continue;
      }

      String s = BeeUtils.concat(1, getWidgetDirection(child).brief(),
          getWidgetWidth(child), getWidgetHeight(child));

      if (child instanceof TilePanel) {
        root.addItem(((TilePanel) child).getTree(s, splitters));
      } else {
        root.addText(s, JreEmulation.getSimpleName(child), DomUtils.getId(child));
      }
    }
    return root;
  }

  public boolean isLeaf() {
    for (Widget w : getChildren()) {
      if (w instanceof TilePanel) {
        return false;
      }
    }
    return true;
  }

  public void move(TilePanel dst) {
    Assert.notNull(dst);

    Widget centerWidget = getCenter();
    ScrollBars centerScroll = getWidgetScroll(centerWidget);

    List<Widget> lst = getTiles(false);
    int c = lst.size();

    if (c <= 0) {
      clear();
      dst.clear();

      if (centerWidget != null) {
        dst.add(centerWidget, centerScroll);
      }
      return;
    }

    Direction[] directions = new Direction[c];
    double[] sizes = new double[c];
    ScrollBars[] scroll = new ScrollBars[c];
    int[] splSizes = new int[c];

    for (int i = 0; i < c; i++) {
      Widget w = lst.get(i);

      directions[i] = getWidgetDirection(w);
      sizes[i] = directions[i].isHorizontal() ? getWidgetWidth(w) : getWidgetHeight(w);
      scroll[i] = getWidgetScroll(w);
      splSizes[i] = getWidgetSplitterSize(w);
    }

    clear();
    dst.clear();

    for (int i = 0; i < c; i++) {
      dst.insert(lst.get(i), directions[i], sizes[i], null, scroll[i], splSizes[i]);
    }
    if (centerWidget != null) {
      dst.add(centerWidget, centerScroll);
    }
    dst.onLayout();
  }

  @Override
  public void onBrowserEvent(Event ev) {
    if (ev.getTypeInt() == Event.ONMOUSEDOWN && isLeaf()) {
      BeeKeeper.getUi().activatePanel(this);
      ev.stopPropagation();
    }
    super.onBrowserEvent(ev);
  }

  protected Widget locateChild(Element elem, boolean tiles, boolean splitters) {
    Widget w = null;
    Map<Element, Widget> children = getChildrenElements(tiles, splitters);

    for (Element p = elem; p != null; p = p.getParentElement().cast()) {
      if (children.containsKey(p)) {
        w = children.get(p);
        break;
      }
    }
    return w;
  }

  private Map<Element, Widget> getChildrenElements(boolean tiles, boolean splitters) {
    Map<Element, Widget> z = new HashMap<Element, Widget>();

    for (Widget w : getChildren()) {
      if (tiles && w instanceof TilePanel) {
        continue;
      }
      if (!splitters && isSplitter(w)) {
        continue;
      }
      z.put(w.getElement(), w);
    }
    return z;
  }

  private List<Widget> getTiles(boolean withCenter) {
    List<Widget> lst = new ArrayList<Widget>();

    for (Widget w : getChildren()) {
      if (isSplitter(w) || (!withCenter && w == getCenter())) {
        continue;
      }
      lst.add(w);
    }
    return lst;
  }
}
