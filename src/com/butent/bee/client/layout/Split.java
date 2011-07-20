package com.butent.bee.client.layout;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.layout.client.Layout.Layer;
import com.google.gwt.user.client.ui.AnimatedLayout;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.LayoutCommand;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.event.HasAfterAddHandler;
import com.butent.bee.client.event.HasBeforeAddHandler;
import com.butent.bee.client.utils.JreEmulation;
import com.butent.bee.client.widget.HorizontalSplitter;
import com.butent.bee.client.widget.Splitter;
import com.butent.bee.client.widget.VerticalSplitter;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Contains core layout management methods.
 */

public class Split extends ComplexPanel implements AnimatedLayout,
    RequiresResize, ProvidesResize, HasId, HasExtendedInfo, HasLayoutCallback {

  private class DockAnimateCommand extends LayoutCommand {
    public DockAnimateCommand(Layout layout) {
      super(layout);
    }

    @Override
    protected void doBeforeLayout() {
      doLayout();
    }
  }

  private static String defaultStyleName = "bee-split";
  private static int defaultSplitterSize = 8;
  
  public static boolean validDirection(Direction direction, boolean allowCenter) {
    if (direction == null) {
      return false;
    }
    if (direction == Direction.CENTER) {
      return allowCenter;
    }
    return EnumSet.of(Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST).contains(
        direction);
  }
  
  private final Unit unit = Unit.PX;
  private Widget center;
  private final Layout layout;
  private final LayoutCommand layoutCmd;

  private final int splitterSize;

  public Split() {
    this(defaultStyleName, defaultSplitterSize);
  }

  public Split(int splitterSize) {
    this(defaultStyleName, splitterSize);
  }

  public Split(String style) {
    this(style, defaultSplitterSize);
  }

  public Split(String style, int splitterSize) {
    setElement(Document.get().createDivElement());
    layout = new Layout(getElement());
    layoutCmd = new DockAnimateCommand(layout);

    this.splitterSize = splitterSize;

    DomUtils.createId(this, getIdPrefix());
    setStyleName(style);
  }

  @Override
  public void add(Widget widget) {
    add(widget, ScrollBars.NONE);
  }

  public void add(Widget widget, ScrollBars scroll) {
    insert(widget, Direction.CENTER, 0, null, scroll, -1);
  }

  public void add(Widget widget, Direction direction, Integer size, ScrollBars scrollBars,
      Integer splSize) {
    Assert.notNull(widget);
    Assert.isTrue(validDirection(direction, false));
    Assert.notNull(size);
    Assert.isPositive(size.doubleValue());
    
    int z = (splSize == null) ? getSplitterSize() : BeeUtils.unbox(splSize);
    insert(widget, direction, size.doubleValue(), null, scrollBars, z);
  }
  
  public void addEast(Widget widget, double size) {
    addEast(widget, size, ScrollBars.NONE);
  }

  public void addEast(Widget widget, double size, ScrollBars scroll) {
    addEast(widget, size, scroll, getSplitterSize());
  }

  public void addEast(Widget widget, double size, ScrollBars scroll, int splSize) {
    insert(widget, Direction.EAST, size, null, scroll, splSize);
  }

  public void addNorth(Widget widget, double size) {
    addNorth(widget, size, ScrollBars.NONE);
  }

  public void addNorth(Widget widget, double size, ScrollBars scroll) {
    addNorth(widget, size, scroll, getSplitterSize());
  }

  public void addNorth(Widget widget, double size, ScrollBars scroll, int splSize) {
    insert(widget, Direction.NORTH, size, null, scroll, splSize);
  }

  public void addSouth(Widget widget, double size) {
    addSouth(widget, size, ScrollBars.NONE);
  }

  public void addSouth(Widget widget, double size, ScrollBars scroll) {
    addSouth(widget, size, scroll, getSplitterSize());
  }

  public void addSouth(Widget widget, double size, ScrollBars scroll, int splSize) {
    insert(widget, Direction.SOUTH, size, null, scroll, splSize);
  }

  public void addWest(Widget widget, double size) {
    addWest(widget, size, ScrollBars.NONE);
  }

  public void addWest(Widget widget, double size, ScrollBars scroll) {
    addWest(widget, size, scroll, getSplitterSize());
  }

  public void addWest(Widget widget, double size, ScrollBars scroll, int splSize) {
    insert(widget, Direction.WEST, size, null, scroll, splSize);
  }

  public void animate(int duration) {
    animate(duration, null);
  }

  public void animate(int duration, final Layout.AnimationCallback callback) {
    layoutCmd.schedule(duration, callback);
  }

  public void forceLayout() {
    layoutCmd.cancel();
    doLayout();
    layout.layout();
    onResize();
  }

  public Widget getCenter() {
    return center;
  }

  public int getCenterHeight() {
    Widget c = getCenter();
    if (c != null) {
      return getWidgetHeight(c);
    }

    int y = getOffsetHeight();
    for (Widget w : getChildren()) {
      if (!isSplitter(w) && isNorthSouth(w)) {
        y -= getWidgetHeight(w);
      }
    }
    return y;
  }

  public int getCenterWidth() {
    Widget c = getCenter();
    if (c != null) {
      return getWidgetWidth(c);
    }

    int x = getOffsetWidth();
    for (Widget w : getChildren()) {
      if (!isSplitter(w) && isWestEast(w)) {
        x -= getWidgetWidth(w);
      }
    }
    return x;
  }

  public List<Widget> getDirectionChildren(Direction dir) {
    List<Widget> lst = new ArrayList<Widget>();

    for (Widget w : getChildren()) {
      if (getWidgetDirection(w) == dir) {
        lst.add(w);
      }
    }
    return lst;
  }

  public List<ExtendedProperty> getDirectionInfo(Direction dir) {
    Assert.notNull(dir);
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    List<Widget> children = getDirectionChildren(dir);
    int c = BeeUtils.length(children);

    PropertyUtils.addExtended(lst, dir.toString(), "Widget Count", c);

    if (c > 0) {
      int i = 0;
      for (Widget w : children) {
        PropertyUtils.appendChildrenToExtended(lst, BeeUtils.progress(++i, c), getChildInfo(w));
      }
    }
    return lst;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "split";
  }

  @Override
  public List<ExtendedProperty> getInfo() {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    PropertyUtils.addChildren(lst, JreEmulation.getSimpleName(this),
        "Id", getId(),
        "Absolute Left", getAbsoluteLeft(),
        "Absolute Top", getAbsoluteTop(),
        "Offset Height", getOffsetHeight(),
        "Offset Width", getOffsetWidth(),
        "Style Name", getStyleName(),
        "Unit", getUnit(),
        "Widget Count", getWidgetCount());

    int i = 0;
    int c = getWidgetCount();
    for (Widget w : getChildren()) {
      String name = BeeUtils.concat(1, BeeUtils.progress(++i, c), getWidgetDirection(w));
      PropertyUtils.appendChildrenToExtended(lst, name, getChildInfo(w));
    }
    return lst;
  }

  public int getSplitterSize() {
    return splitterSize;
  }

  public Element getWidgetContainerElement(Widget child) {
    assertIsChild(child);
    return ((LayoutData) child.getLayoutData()).layer.getContainerElement();
  }

  public Direction getWidgetDirection(Widget child) {
    assertIsChild(child);
    if (child.getParent() != this) {
      return null;
    }
    return ((LayoutData) child.getLayoutData()).direction;
  }

  public int getWidgetHeight(Widget child) {
    return getWidgetContainerElement(child).getOffsetHeight();
  }

  public ScrollBars getWidgetScroll(Widget child) {
    return StyleUtils.getScroll(getWidgetContainerElement(child));
  }

  public int getWidgetSize(Widget child) {
    assertIsChild(child);
    LayoutData data = (LayoutData) child.getLayoutData();

    return (int) data.size;
  }

  public int getWidgetSplitterSize(Widget child) {
    assertIsChild(child);
    Splitter splitter = getAssociatedSplitter(child);

    if (splitter == null) {
      return -1;
    } else {
      return splitter.getSize();
    }
  }

  public int getWidgetWidth(Widget child) {
    return getWidgetContainerElement(child).getOffsetWidth();
  }

  public void hideSplitter(Widget child) {
    assertIsChild(child);
    Splitter splitter = getAssociatedSplitter(child);

    if (splitter != null) {
      splitter.setVisible(false);
    }
  }

  public void insert(Widget child, Direction direction, double size,
      Widget before, ScrollBars scroll, int splSize) {
    assertIsChild(before);

    if (before == null) {
      Assert.isTrue(center == null, "No widget may be added after the CENTER widget");
    } else {
      Assert.isTrue(direction != Direction.CENTER, "A CENTER widget must always be added last");
    }

    Widget w = child;
    if (w instanceof HasBeforeAddHandler) {
      w = ((HasBeforeAddHandler) w).onBeforeAdd(this);
    }

    w.removeFromParent();

    WidgetCollection children = getChildren();
    if (before == null) {
      children.add(w);
    } else {
      int index = children.indexOf(before);
      children.insert(w, index);
    }

    Layer layer = layout.attachChild(w.getElement(),
        (before != null) ? before.getElement() : null, w);
    LayoutData data = new LayoutData(direction, size, layer);
    w.setLayoutData(data);

    adopt(w);

    Element container = layer.getContainerElement();

    String pfx;
    if (isSplitter(w)) {
      pfx = (w instanceof HorizontalSplitter) ? "hor" : "vert";
    } else {
      pfx = BeeUtils.transform(direction).toLowerCase();
    }

    container.setId(DomUtils.createUniqueId("layer-" + pfx));

    if (!isSplitter(w)) {
      if (scroll != null && !(ScrollBars.NONE.equals(scroll))) {
        StyleUtils.autoScroll(container, scroll);
      }

      if (direction == Direction.CENTER) {
        center = w;
      } else if (splSize > 0) {
        insertSplitter(w, container, before, splSize);
      }
    }

    if (w instanceof HasAfterAddHandler) {
      ((HasAfterAddHandler) w).onAfterAdd(this);
    }
    animate(0);
  }

  public void insertEast(Widget widget, double size, Widget before, ScrollBars sb, int splSize) {
    insert(widget, Direction.EAST, size, before, sb, splSize);
  }

  public void insertNorth(Widget widget, double size, Widget before, ScrollBars sb, int splSize) {
    insert(widget, Direction.NORTH, size, before, sb, splSize);
  }

  public void insertSouth(Widget widget, double size, Widget before, ScrollBars sb, int splSize) {
    insert(widget, Direction.SOUTH, size, before, sb, splSize);
  }

  public void insertWest(Widget widget, double size, Widget before, ScrollBars sb, int splSize) {
    insert(widget, Direction.WEST, size, before, sb, splSize);
  }

  public void onLayout() {
    forceLayout();
  }

  public void onResize() {
    for (Widget child : getChildren()) {
      if (child instanceof RequiresResize) {
        ((RequiresResize) child).onResize();
      }
    }
  }

  @Override
  public boolean remove(Widget w) {
    Assert.notNull(w);

    Splitter splitter = null;
    if (!isSplitter(w)) {
      splitter = getAssociatedSplitter(w);
    }

    boolean removed = super.remove(w);
    if (removed) {
      if (w == center) {
        center = null;
      }

      LayoutData data = (LayoutData) w.getLayoutData();
      layout.removeChild(data.layer);

      if (splitter != null) {
        remove(splitter);
      }
    }
    return removed;
  }

  public void setDirectionSize(Direction direction, double size) {
    Assert.isTrue(validDirection(direction, false));

    for (Widget w : getChildren()) {
      if (!isSplitter(w) && getWidgetDirection(w) == direction) {
        setWidgetSize(w, size);
      }
    }
  }

  public boolean setDirectionSize(String s, double size) {
    Direction dir = DomUtils.getDirection(s);

    if (validDirection(dir, false)) {
      setDirectionSize(dir, size);
      return true;
    } else {
      return false;
    }
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setWidgetMinSize(Widget child, int minSize) {
    assertIsChild(child);
    Splitter splitter = getAssociatedSplitter(child);

    if (splitter != null) {
      splitter.setMinSize(minSize);
    }
  }

  public void setWidgetSize(Widget widget, double size) {
    assertIsChild(widget);
    LayoutData data = (LayoutData) widget.getLayoutData();

    Assert.isTrue(data.direction != Direction.CENTER,
        "The size of the center widget can not be updated.");
    data.size = size;

    forceLayout();
  }

  public void updateCenter(Widget widget, ScrollBars scroll) {
    Assert.notNull(widget);

    Widget w = getCenter();
    if (w != null) {
      remove(w);
    }
    add(widget, scroll);
  }

  protected Unit getUnit() {
    return unit;
  }

  protected boolean isSplitter(Widget w) {
    return w instanceof Splitter;
  }

  @Override
  protected void onLoad() {
    layout.onAttach();
  }

  @Override
  protected void onUnload() {
    layout.onDetach();
  }

  void assertIsChild(Widget widget) {
    Assert.isTrue((widget == null) || (widget.getParent() == this),
        "The specified widget is not a child of this panel");
  }

  private void doLayout() {
    double left = 0, top = 0, right = 0, bottom = 0;

    for (Widget child : getChildren()) {
      LayoutData data = (LayoutData) child.getLayoutData();
      Layer layer = data.layer;

      switch (data.direction) {
        case NORTH:
          layer.setLeftRight(left, unit, right, unit);
          layer.setTopHeight(top, unit, data.size, unit);
          top += data.size;
          break;

        case SOUTH:
          layer.setLeftRight(left, unit, right, unit);
          layer.setBottomHeight(bottom, unit, data.size, unit);
          bottom += data.size;
          break;

        case WEST:
          layer.setTopBottom(top, unit, bottom, unit);
          layer.setLeftWidth(left, unit, data.size, unit);
          left += data.size;
          break;

        case EAST:
          layer.setTopBottom(top, unit, bottom, unit);
          layer.setRightWidth(right, unit, data.size, unit);
          right += data.size;
          break;

        case CENTER:
          layer.setLeftRight(left, unit, right, unit);
          layer.setTopBottom(top, unit, bottom, unit);
          break;
      }
    }
  }

  private Splitter getAssociatedSplitter(Widget child) {
    int idx = getWidgetIndex(child);
    if (idx > -1 && idx < getWidgetCount() - 1) {
      Widget splitter = getWidget(idx + 1);
      if (isSplitter(splitter)) {
        return (Splitter) splitter;
      }
    }
    return null;
  }

  private List<Property> getChildInfo(Widget w) {
    List<Property> lst = new ArrayList<Property>();

    if (w instanceof HasId) {
      PropertyUtils.addProperty(lst, "Id", ((HasId) w).getId());
    }

    PropertyUtils.addProperties(lst,
        "Class", JreEmulation.getSimpleName(w),
        "Absolute Left", w.getAbsoluteLeft(),
        "Absolute Top", w.getAbsoluteTop(),
        "Offset Height", w.getOffsetHeight(),
        "Offset Width", w.getOffsetWidth(),
        "Style Name", w.getStyleName(),
        "Title", w.getTitle(),
        "Visible", w.isVisible());

    if (w instanceof HasWidgets) {
      PropertyUtils.addProperty(lst, "Children Count", DomUtils.getWidgetCount((HasWidgets) w));
    }

    Element container = getWidgetContainerElement(w);
    if (container != null) {
      PropertyUtils.addProperties(lst,
          "Container Offset Height", container.getOffsetHeight(),
          "Container Offset Width", container.getOffsetWidth(),
          "Container Client Height", container.getClientHeight(),
          "Container Client Width", container.getClientWidth());
    }

    if (isSplitter(w)) {
      Splitter bspl = (Splitter) w;
      PropertyUtils.addProperties(lst,
          "Reverse", bspl.isReverse(),
          "Size", bspl.getSize(),
          "Min Size", bspl.getMinSize(),
          "Absolute Position", bspl.getAbsolutePosition(),
          "Target Position", bspl.getTargetPosition(),
          "Target Size", bspl.getTargetSize());
    }
    return lst;
  }

  private void insertSplitter(Widget widget, Element container, Widget before, int size) {
    LayoutData ld = (LayoutData) widget.getLayoutData();
    Splitter splitter = null;

    switch (ld.direction) {
      case WEST:
        splitter = new HorizontalSplitter(widget, container, false, size);
        break;
      case EAST:
        splitter = new HorizontalSplitter(widget, container, true, size);
        break;
      case NORTH:
        splitter = new VerticalSplitter(widget, container, false, size);
        break;
      case SOUTH:
        splitter = new VerticalSplitter(widget, container, true, size);
        break;
      default:
        Assert.untouchable();
    }
    insert(splitter, ld.direction, size, before, null, -1);
  }

  private boolean isNorthSouth(Widget w) {
    if (w == null) {
      return false;
    }
    Direction dir = getWidgetDirection(w);
    return dir == Direction.NORTH || dir == Direction.SOUTH;
  }

  private boolean isWestEast(Widget w) {
    if (w == null) {
      return false;
    }
    Direction dir = getWidgetDirection(w);
    return dir == Direction.WEST || dir == Direction.EAST;
  }
}
