package com.butent.bee.client.screen;

import com.google.common.collect.Lists;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.client.view.View;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

class TilePanel extends Split implements HasSelectionHandlers<TilePanel>, HasCaption {

  private static class BlankTile extends CustomDiv {
    private BlankTile() {
      super("bee-BlankTile");
    }

    @Override
    public String getIdPrefix() {
      return "blank";
    }
  }

  private static final int MIN_SIZE = 20;

  private static final String ACTIVE_BLANK = "bee-activeBlank";
  private static final String ACTIVE_CONTENT = "bee-activeContent";

  static TilePanel getParentTile(Widget widget) {
    for (Widget parent = widget; parent != null; parent = parent.getParent()) {
      if (parent instanceof TilePanel) {
        return (TilePanel) parent;
      }
    }
    return null;
  }
  
  static TilePanel newBlankTile(SelectionHandler<TilePanel> handler) {
    return newTile(new BlankTile(), ScrollBars.NONE, handler);
  }

  static TilePanel newTile(Widget widget, ScrollBars scroll, SelectionHandler<TilePanel> handler) {
    TilePanel tile = new TilePanel();
    tile.add(widget, scroll);
    if (handler != null) {
      tile.addSelectionHandler(handler);
    }
    return tile;
  }

  private static TilePanel locateActiveChild(TilePanel parent) {
    if (parent.isActive()) {
      return parent;
    }

    for (Widget child : parent.getChildren()) {
      if (child instanceof TilePanel) {
        TilePanel tile = locateActiveChild((TilePanel) child);
        if (tile != null) {
          return tile;
        }
      }
    }
    return null;
  }

  private boolean active = false;

  private TilePanel() {
    super(5);

    addStyleName("bee-tile");
    sinkEvents(Event.ONMOUSEDOWN);
  }

  @Override
  public HandlerRegistration addSelectionHandler(SelectionHandler<TilePanel> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

  @Override
  public String getCaption() {
    String caption = null;

    for (Widget child : getChildren()) {
      if (child instanceof HasCaption) {
        caption = ((HasCaption) child).getCaption();

      } else if (child instanceof View) {
        Presenter presenter = ((View) child).getViewPresenter();
        if (presenter != null) {
          caption = presenter.getCaption();
        }
      }

      if (!BeeUtils.isEmpty(caption)) {
        break;
      }
    }
    return caption;
  }

  @Override
  public String getIdPrefix() {
    return "tile";
  }

  @Override
  public void onBrowserEvent(Event ev) {
    if (!isActive() && EventUtils.isMouseDown(ev.getType()) && isLeaf()) {
      ev.stopPropagation();
      activate();
    }
    super.onBrowserEvent(ev);
  }

  void activate() {
    if (isActive()) {
      return;
    }

    deactivate();

    setActiveStyle(true);
    setActive(true);

    SelectionEvent.fire(this, this);
  }

  void addTile(Direction direction, SelectionHandler<TilePanel> handler) {
    int size = direction.isHorizontal() ? getCenterWidth() : getCenterHeight();
    size = Math.round((size - getSplitterSize()) / 2);
    if (size < MIN_SIZE) {
      Global.showError("Sub-Planck length is not allowed in this universe");
      return;
    }

    deactivate();

    Widget oldCenter = getCenter();
    TilePanel newCenter;

    if (oldCenter != null) {
      ScrollBars scroll = getWidgetScroll(oldCenter);

      Global.setTemporaryDetach(true);
      remove(oldCenter);
      Global.setTemporaryDetach(false);

      newCenter = newTile(oldCenter, scroll, handler);
      newCenter.onLayout();
    } else {
      newCenter = newBlankTile(handler);
    }

    TilePanel tile = newBlankTile(handler);

    insert(tile, direction, size, null, null, getSplitterSize());
    add(newCenter);

    tile.activate();
  }

  void close() {
    boolean wasActive = isActive();

    if (!isBlank()) {
      if (wasActive) {
        deactivate();
      }
      blank();
      if (wasActive) {
        activate();
      }
      return;
    }

    if (isRoot()) {
      return;
    }

    TilePanel parent = (TilePanel) getParent();
    TilePanel entangled = null;

    for (Widget sibling : parent.getChildren()) {
      if (sibling instanceof TilePanel && sibling != this) {
        entangled = (TilePanel) sibling;
        break;
      }
    }
    if (entangled == null) {
      BeeKeeper.getLog().severe("entangled tile not found");
      return;
    }

    if (wasActive) {
      deactivate();
    }

    Global.setTemporaryDetach(true);
    entangled.moveTo(parent);
    Global.setTemporaryDetach(false);

    if (wasActive) {
      while (parent.getCenter() instanceof TilePanel) {
        parent = (TilePanel) parent.getCenter();
      }
      parent.activate();
    }
  }

  boolean closeable() {
    return !isRoot() || !isBlank();
  }
  
  TilePanel getActiveTile() {
    return isActive() ? this : locateActiveChild(getRoot());
  }

  Widget getContent() {
    return isBlank() ? null : getCenter();
  }

  TreeItem getTree(String prefix) {
    TreeItem root = new TreeItem(BeeUtils.joinWords(prefix, getId()));

    for (Widget child : getChildren()) {
      if (isSplitter(child)) {
        continue;
      }

      String s = BeeUtils.joinWords(getWidgetDirection(child).brief(),
          getWidgetWidth(child), getWidgetHeight(child));

      if (child instanceof TilePanel) {
        if (((TilePanel) child).isActive()) {
          root.addItem("active");
        }
        root.addItem(((TilePanel) child).getTree(s));
      } else {
        root.addItem(BeeUtils.joinWords(s, NameUtils.getName(child), DomUtils.getId(child)));
      }
    }
    return root;
  }
  
  void updateContent(Widget widget, ScrollBars scroll) {
    boolean wasActive = isActive();
    if (wasActive) {
      deactivate();
    }

    clear();
    add(widget, scroll);
    
    if (wasActive) {
      activate();
    }
  }

  private void blank() {
    clear();
    add(new BlankTile());
  }

  private void deactivate() {
    TilePanel tile = getActiveTile();

    if (tile != null) {
      tile.setActiveStyle(false);
      tile.setActive(false);
    }
  }

  private TilePanel getRoot() {
    return isRoot() ? this : ((TilePanel) getParent()).getRoot();
  }
  
  private boolean isActive() {
    return active;
  }

  private boolean isBlank() {
    return getCenter() instanceof BlankTile;
  }

  private boolean isLeaf() {
    for (Widget child : getChildren()) {
      if (child instanceof TilePanel) {
        return false;
      }
    }
    return true;
  }

  private boolean isRoot() {
    return !(getParent() instanceof TilePanel);
  }

  private void moveTo(TilePanel parent) {
    Widget centerWidget = getCenter();
    ScrollBars centerScroll = getWidgetScroll(centerWidget);

    List<Widget> children = Lists.newArrayList();
    for (Widget child : getChildren()) {
      if (!isSplitter(child) && child != centerWidget) {
        children.add(child);
      }
    }

    int c = children.size();
    if (c <= 0) {
      clear();
      parent.clear();

      if (centerWidget != null) {
        parent.add(centerWidget, centerScroll);
      }
      return;
    }

    Direction[] directions = new Direction[c];
    double[] sizes = new double[c];
    ScrollBars[] scroll = new ScrollBars[c];
    int[] splSizes = new int[c];

    for (int i = 0; i < c; i++) {
      Widget child = children.get(i);

      directions[i] = getWidgetDirection(child);
      sizes[i] = directions[i].isHorizontal() ? getWidgetWidth(child) : getWidgetHeight(child);
      scroll[i] = getWidgetScroll(child);
      splSizes[i] = getWidgetSplitterSize(child);
    }

    clear();
    parent.clear();

    for (int i = 0; i < c; i++) {
      parent.insert(children.get(i), directions[i], sizes[i], null, scroll[i], splSizes[i]);
    }
    if (centerWidget != null) {
      parent.add(centerWidget, centerScroll);
    }
    parent.onLayout();
  }

  private void setActive(boolean active) {
    this.active = active;
  }

  private void setActiveStyle(boolean add) {
    if (isRoot()) {
      return;
    }

    Widget widget = getCenter();

    if (widget instanceof BlankTile) {
      widget.setStyleName(ACTIVE_BLANK, add);
    } else if (widget != null) {
      if (add) {
        getWidgetContainerElement(widget).addClassName(ACTIVE_CONTENT);
      } else {
        getWidgetContainerElement(widget).removeClassName(ACTIVE_CONTENT);
      }
    }
  }
}
