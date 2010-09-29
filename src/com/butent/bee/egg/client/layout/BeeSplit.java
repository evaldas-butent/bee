package com.butent.bee.egg.client.layout;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
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

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.utils.JreEmulation;
import com.butent.bee.egg.client.widget.BeeHSplitter;
import com.butent.bee.egg.client.widget.BeeSplitter;
import com.butent.bee.egg.client.widget.BeeVSplitter;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.HasExtendedInfo;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.StringProp;
import com.butent.bee.egg.shared.utils.SubProp;

import java.util.ArrayList;
import java.util.List;

public class BeeSplit extends ComplexPanel implements AnimatedLayout,
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

  private final Unit unit = Unit.PX;
  private Widget center;
  private final Layout layout;
  private final LayoutCommand layoutCmd;
  private int splitterSize = 8;

  public BeeSplit() {
    setElement(Document.get().createDivElement());
    layout = new Layout(getElement());
    layoutCmd = new DockAnimateCommand(layout);
    setStyleName("bee-SplitPanel");
    createId();
  }

  @Override
  public void add(Widget widget) {
    add(widget, false);
  }

  public void add(Widget widget, boolean scroll) {
    insert(widget, BeeDirection.CENTER, 0, null, scroll);
  }
  
  public void addEast(Widget widget, double size) {
    addEast(widget, size, false);
  }

  public void addEast(Widget widget, double size, boolean scroll) {
    insert(widget, BeeDirection.EAST, size, null, scroll);
  }
  
  public void addNorth(Widget widget, double size) {
    addNorth(widget, size, false);
  }

  public void addNorth(Widget widget, double size, boolean scroll) {
    insert(widget, BeeDirection.NORTH, size, null, scroll);
  }
  
  public void addSouth(Widget widget, double size) {
    addSouth(widget, size, false);
  }

  public void addSouth(Widget widget, double size, boolean scroll) {
    insert(widget, BeeDirection.SOUTH, size, null, scroll);
  }

  public void addWest(Widget widget, double size) {
    addWest(widget, size, false);
  }

  public void addWest(Widget widget, double size, boolean scroll) {
    insert(widget, BeeDirection.WEST, size, null, scroll);
  }
  
  public void animate(int duration) {
    animate(duration, null);
  }

  public void animate(int duration, final Layout.AnimationCallback callback) {
    layoutCmd.schedule(duration, callback);
  }

  public void createId() {
    DomUtils.createId(this, "split");
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

  public List<SubProp> getDirectionInfo(BeeDirection dir) {
    Assert.notNull(dir);
    List<SubProp> lst = new ArrayList<SubProp>();

    List<Widget> children = getDirectionChildren(dir);
    int c = BeeUtils.length(children);

    PropUtils.addSub(lst, dir.toString(), "Widget Count", c);

    if (c > 0) {
      int i = 0;
      for (Widget w : children) {
        PropUtils.appendString(lst, BeeUtils.progress(++i, c), getChildInfo(w));
      }
    }

    return lst;
  }
  
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public List<SubProp> getInfo() {
    List<SubProp> lst = new ArrayList<SubProp>();

    PropUtils.addRoot(lst, JreEmulation.getSimpleName(this), "Id", getId(),
        "Absolute Left", getAbsoluteLeft(), "Absolute Top", getAbsoluteTop(),
        "Offset Height", getOffsetHeight(), "Offset Width", getOffsetWidth(),
        "Style Name", getStyleName(), "Unit", getUnit(), "Widget Count",
        getWidgetCount());

    int i = 0;
    int c = getWidgetCount();
    for (Widget w : getChildren()) {
      String name = BeeUtils.concat(1, BeeUtils.progress(++i, c),
          getWidgetDirection(w));
      PropUtils.appendString(lst, name, getChildInfo(w));
    }

    return lst;
  }
  
  public Element getWidgetContainerElement(Widget child) {
    assertIsChild(child);
    return ((BeeLayoutData) child.getLayoutData()).layer.getContainerElement();
  }

  public BeeDirection getWidgetDirection(Widget child) {
    assertIsChild(child);
    if (child.getParent() != this) {
      return null;
    }
    return ((BeeLayoutData) child.getLayoutData()).direction;
  }

  public int getWidgetHeight(Widget child) {
    return getWidgetContainerElement(child).getOffsetHeight();
  }

  public int getWidgetWidth(Widget child) {
    return getWidgetContainerElement(child).getOffsetWidth();
  }

  public void insertEast(Widget widget, double size, Widget before, boolean scroll) {
    insert(widget, BeeDirection.EAST, size, before, scroll);
  }

  public void insertNorth(Widget widget, double size, Widget before, boolean scroll) {
    insert(widget, BeeDirection.NORTH, size, before, scroll);
  }

  public void insertSouth(Widget widget, double size, Widget before, boolean scroll) {
    insert(widget, BeeDirection.SOUTH, size, before, scroll);
  }

  public void insertWest(Widget widget, double size, Widget before, boolean scroll) {
    insert(widget, BeeDirection.WEST, size, before, scroll);
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

    BeeSplitter splitter = null;
    if (!isSplitter(w)) {
      splitter = getAssociatedSplitter(w);
    }

    boolean removed = super.remove(w);
    if (removed) {
      if (w == center) {
        center = null;
      }

      BeeLayoutData data = (BeeLayoutData) w.getLayoutData();
      layout.removeChild(data.layer);

      if (splitter != null) {
        remove(splitter);
      }
    }

    return removed;
  }

  public void setDirectionSize(BeeDirection direction, double size) {
    Assert.isTrue(validDirection(direction, false));

    for (Widget w : getChildren()) {
      if (!isSplitter(w) && getWidgetDirection(w) == direction) {
        setWidgetSize(w, size);
      }
    }
  }

  public boolean setDirectionSize(String s, double size) {
    BeeDirection dir = DomUtils.getDirection(s);

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
    BeeSplitter splitter = getAssociatedSplitter(child);

    if (splitter != null) {
      splitter.setMinSize(minSize);
    }
  }

  public void setWidgetSize(Widget widget, double size) {
    assertIsChild(widget);
    BeeLayoutData data = (BeeLayoutData) widget.getLayoutData();

    Assert.isTrue(data.direction != BeeDirection.CENTER,
        "The size of the center widget can not be updated.");

    data.size = size;

    animate(0);
  }
  
  public void updateCenter(Widget widget, boolean scroll) {
    Assert.notNull(widget);

    Widget w = getCenter();
    if (w != null) {
      remove(w);
    }

    add(widget, scroll);
  }

  public boolean validDirection(BeeDirection direction, boolean center) {
    if (direction == BeeDirection.CENTER) {
      return center;
    }
    return BeeUtils.inList(direction, BeeDirection.EAST, BeeDirection.NORTH,
        BeeDirection.SOUTH, BeeDirection.WEST);
  }
  
  protected Unit getUnit() {
    return unit;
  }

  protected void insert(Widget widget, BeeDirection direction, double size,
      Widget before, boolean scroll) {
    assertIsChild(before);

    if (before == null) {
      Assert.isTrue(center == null,
          "No widget may be added after the CENTER widget");
    } else {
      Assert.isTrue(direction != BeeDirection.CENTER,
          "A CENTER widget must always be added last");
    }

    widget.removeFromParent();

    WidgetCollection children = getChildren();
    if (before == null) {
      children.add(widget);
    } else {
      int index = children.indexOf(before);
      children.insert(widget, index);
    }

    Layer layer = layout.attachChild(widget.getElement(), (before != null)
        ? before.getElement() : null, widget);
    BeeLayoutData data = new BeeLayoutData(direction, size, layer);
    widget.setLayoutData(data);

    adopt(widget);
    
    Element container = layer.getContainerElement();
    
    String pfx;
    if (isSplitter(widget)) {
      pfx = (widget instanceof BeeHSplitter) ? "hor" : "vert";
    } else {
      pfx = BeeUtils.transform(direction).toLowerCase();
    }

    container.setId(DomUtils.createUniqueId("layer-" + pfx));

    if (!isSplitter(widget)) {
      if (scroll) {
        container.getStyle().setOverflow(Overflow.AUTO);
      }

      if (direction == BeeDirection.CENTER) {
        center = widget;
      } else {
        insertSplitter(widget, container, before);
      }
    }

    animate(0);
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
      BeeLayoutData data = (BeeLayoutData) child.getLayoutData();
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

  private BeeSplitter getAssociatedSplitter(Widget child) {
    int idx = getWidgetIndex(child);
    if (idx > -1 && idx < getWidgetCount() - 1) {
      Widget splitter = getWidget(idx + 1);
      if (isSplitter(splitter)) {
        return (BeeSplitter) splitter;
      }
    }
    return null;
  }

  private List<StringProp> getChildInfo(Widget w) {
    List<StringProp> lst = new ArrayList<StringProp>();

    if (w instanceof HasId) {
      PropUtils.addString(lst, "Id", ((HasId) w).getId());
    }

    PropUtils.addString(lst, "Class", JreEmulation.getSimpleName(w),
        "Absolute Left", w.getAbsoluteLeft(), "Absolute Top",
        w.getAbsoluteTop(), "Offset Height", w.getOffsetHeight(),
        "Offset Width", w.getOffsetWidth(),
        "Style Name", w.getStyleName(),
        "Title", w.getTitle(), "Visible", w.isVisible());

    if (w instanceof HasWidgets) {
      PropUtils.addString(lst, "Children Count",
          DomUtils.getWidgetCount((HasWidgets) w));
    }
    
    Element container = getWidgetContainerElement(w);
    if (container != null) {
      PropUtils.addString(lst,
          "Container Offset Height", container.getOffsetHeight(),
          "Container Offset Width", container.getOffsetWidth(),
          "Container Client Height", container.getClientHeight(),
          "Container Client Width", container.getClientWidth());
    }
    
    if (isSplitter(w)) {
      BeeSplitter bspl = (BeeSplitter) w;
      PropUtils.addString(lst,
          "Reverse", bspl.isReverse(),
          "Size", bspl.getSize(),
          "Min Size", bspl.getMinSize(),
          "Absolute Position", bspl.getAbsolutePosition(),
          "Target Position", bspl.getTargetPosition(),
          "Target Size", bspl.getTargetSize());
    }

    return lst;
  }

  private List<Widget> getDirectionChildren(BeeDirection dir) {
    List<Widget> lst = new ArrayList<Widget>();

    for (Widget w : getChildren()) {
      if (getWidgetDirection(w) == dir) {
        lst.add(w);
      }
    }

    return lst;
  }

  private void insertSplitter(Widget widget, Element container, Widget before) {
    Assert.isTrue(getChildren().size() > 0,
        "Can't add a splitter before any children");

    BeeLayoutData ld = (BeeLayoutData) widget.getLayoutData();
    BeeSplitter splitter = null;

    switch (ld.direction) {
      case WEST:
        splitter = new BeeHSplitter(widget, container, false, splitterSize);
        break;
      case EAST:
        splitter = new BeeHSplitter(widget, container, true, splitterSize);
        break;
      case NORTH:
        splitter = new BeeVSplitter(widget, container, false, splitterSize);
        break;
      case SOUTH:
        splitter = new BeeVSplitter(widget, container, true, splitterSize);
        break;
      default:
        Assert.untouchable();
    }

    insert(splitter, ld.direction, splitterSize, before, false);
  }

  private boolean isNorthSouth(Widget w) {
    if (w == null) {
      return false;
    }
    BeeDirection dir = getWidgetDirection(w);
    return dir == BeeDirection.NORTH || dir == BeeDirection.SOUTH;
  }
  
  private boolean isSplitter(Widget w) {
    return w instanceof BeeSplitter;
  }

  private boolean isWestEast(Widget w) {
    if (w == null) {
      return false;
    }
    BeeDirection dir = getWidgetDirection(w);
    return dir == BeeDirection.WEST || dir == BeeDirection.EAST;
  }
  
}
