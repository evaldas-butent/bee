package com.butent.bee.client.layout;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragEvent;
import com.google.gwt.event.dom.client.DragHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.HasAllDragAndDropHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

public class Split extends ComplexPanel implements RequiresResize, ProvidesResize,
    IdentifiableWidget, HasExtendedInfo, HasAllDragAndDropHandlers {

  private class HorizontalSplitter extends Splitter {

    private HorizontalSplitter(int size, boolean reverse) {
      super(size, reverse);

      getElement().getStyle().setWidth(size, Unit.PX);
      addStyleName("bee-HSplitter");
    }

    @Override
    public int getAbsolutePosition() {
      return getAbsoluteLeft();
    }

    @Override
    public int getEventPosition(Event event) {
      return event.getClientX();
    }

    @Override
    public String getIdPrefix() {
      return "h-splitter";
    }

    @Override
    public int getPosition(Widget widget) {
      return widget.getAbsoluteLeft();
    }

    @Override
    public int getSize(Widget widget) {
      return widget.getOffsetWidth();
    }
  }

  private static class LayoutData {
    private final Direction direction;
    private int size;

    private LayoutData(Direction direction, int size) {
      super();
      this.direction = direction;
      this.size = Math.max(size, 0);
    }

    private Direction getDirection() {
      return direction;
    }

    private int getSize() {
      return size;
    }

    private void setSize(int size) {
      this.size = Math.max(size, 0);
    }
  }

  private abstract class Splitter extends CustomDiv {
    
    private final int size;
    private final boolean reverse;

    private int minSize;
    private int maxSize;

    private boolean mouseDown = false;
    private int offset = 0;
    private Widget target;

    private Splitter(int size, boolean reverse) {
      super();

      this.size = size;
      this.reverse = reverse;

      this.minSize = size * 2;

      addStyleName("bee-Splitter");

      sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEMOVE);
    }

    @Override
    public void onBrowserEvent(Event event) {
      switch (event.getTypeInt()) {
        case Event.ONMOUSEDOWN:
          if (startResizing()) {
            setMouseDown(true);
            setOffset(getEventPosition(event) - getAbsolutePosition());

            Event.setCapture(getElement());

            event.preventDefault();
            event.stopPropagation();
          }
          break;

        case Event.ONMOUSEUP:
          if (isMouseDown()) {
            setMouseDown(false);
            Event.releaseCapture(getElement());

            event.preventDefault();
            event.stopPropagation();

            endResizing();
          }
          break;

        case Event.ONMOUSEMOVE:
          if (isMouseDown()) {
            event.preventDefault();
            event.stopPropagation();

            int z;
            if (isReverse()) {
              z = getPosition(target) + getSize(target) - getEventPosition(event) - getOffset();
            } else {
              z = getEventPosition(event) - getPosition(target) - getOffset();
            }
            setTargetSize(z);
          }
          break;
      }
    }

    protected abstract int getAbsolutePosition();

    protected abstract int getEventPosition(Event event);

    protected abstract int getPosition(Widget widget);

    protected abstract int getSize(Widget widget);

    private void endResizing() {
      setTarget(null);
      Split.this.onResize();
    }

    private int getMaxSize() {
      return maxSize;
    }

    private int getMinSize() {
      return minSize;
    }

    private int getOffset() {
      return offset;
    }

    private int getSize() {
      return size;
    }

    private Widget getTarget() {
      return target;
    }

    private boolean isMouseDown() {
      return mouseDown;
    }

    private boolean isReverse() {
      return reverse;
    }

    private void setMaxSize(int maxSize) {
      this.maxSize = maxSize;
    }

    private void setMinSize(int minSize) {
      this.minSize = minSize;
    }

    private void setMouseDown(boolean mouseDown) {
      this.mouseDown = mouseDown;
    }

    private void setOffset(int offset) {
      this.offset = offset;
    }

    private void setTarget(Widget target) {
      this.target = target;
    }

    private void setTargetSize(int size) {
      int z = BeeUtils.clamp(size, getMinSize(), getMaxSize());

      LayoutData data = Split.this.getLayoutData(getTarget());
      if (data == null || z == data.getSize()) {
        return;
      }

      data.setSize(z);
      Split.this.layoutChildren();
    }

    private boolean startResizing() {
      if (Split.this.getCenter() == null) {
        return false;
      }

      int index = Split.this.getWidgetIndex(this);
      if (index <= 0) {
        return false;
      }
      
      setTarget(Split.this.getWidget(index - 1));
      
      int targetSize = getSize(getTarget());
      int centerSize = getSize(Split.this.getCenter().asWidget());
      setMaxSize(targetSize + centerSize - size * 2);

      return true;
    }
  }

  private class VerticalSplitter extends Splitter {

    private VerticalSplitter(int size, boolean reverse) {
      super(size, reverse);

      getElement().getStyle().setHeight(size, Unit.PX);
      addStyleName("bee-VSplitter");
    }

    @Override
    public int getAbsolutePosition() {
      return getAbsoluteTop();
    }

    @Override
    public int getEventPosition(Event event) {
      return event.getClientY();
    }

    @Override
    public String getIdPrefix() {
      return "v-splitter";
    }

    @Override
    public int getPosition(Widget widget) {
      return widget.getAbsoluteTop();
    }

    @Override
    public int getSize(Widget widget) {
      return widget.getOffsetHeight();
    }
  }

  private static final int DEFAULT_SPLITTER_SIZE = 8;

  private static final String STYLE_NAME = "bee-Split";
  private static final String STYLE_CHILD = STYLE_NAME + "Child";

  public static boolean validDirection(Direction direction, boolean allowCenter) {
    if (direction == null) {
      return false;
    } else if (direction == Direction.CENTER) {
      return allowCenter;
    } else {
      return true;
    }
  }

  private final Unit unit = Unit.PX;

  private final int splitterSize;

  private IdentifiableWidget center = null;

  private boolean providesResize = true;

  public Split() {
    this(DEFAULT_SPLITTER_SIZE);
  }

  public Split(int splitterSize) {
    setElement(Document.get().createDivElement());

    this.splitterSize = splitterSize;

    DomUtils.createId(this, getIdPrefix());
    addStyleName(STYLE_NAME);
  }

  public void add(IdentifiableWidget widget, Direction direction, Integer size, Integer splSize) {
    Assert.notNull(widget);
    Assert.isTrue(validDirection(direction, false));
    Assert.notNull(size);

    int z = (splSize == null) ? getSplitterSize() : BeeUtils.unbox(splSize);
    insert(widget, direction, size, null, z);
  }

  @Override
  public void add(Widget widget) {
    if (widget instanceof IdentifiableWidget) {
      insert((IdentifiableWidget) widget, Direction.CENTER, 0, null, BeeConst.UNDEF);
    } else {
      Assert.unsupported("only IdentifiableWidget can be added to Split panel");
    }
  }

  @Override
  public HandlerRegistration addDragEndHandler(DragEndHandler handler) {
    return addBitlessDomHandler(handler, DragEndEvent.getType());
  }

  @Override
  public HandlerRegistration addDragEnterHandler(DragEnterHandler handler) {
    return addBitlessDomHandler(handler, DragEnterEvent.getType());
  }

  @Override
  public HandlerRegistration addDragHandler(DragHandler handler) {
    return addBitlessDomHandler(handler, DragEvent.getType());
  }

  @Override
  public HandlerRegistration addDragLeaveHandler(DragLeaveHandler handler) {
    return addBitlessDomHandler(handler, DragLeaveEvent.getType());
  }

  @Override
  public HandlerRegistration addDragOverHandler(DragOverHandler handler) {
    return addBitlessDomHandler(handler, DragOverEvent.getType());
  }

  @Override
  public HandlerRegistration addDragStartHandler(DragStartHandler handler) {
    return addBitlessDomHandler(handler, DragStartEvent.getType());
  }

  @Override
  public HandlerRegistration addDropHandler(DropHandler handler) {
    return addBitlessDomHandler(handler, DropEvent.getType());
  }

  public void addEast(IdentifiableWidget widget, int size) {
    addEast(widget, size, getSplitterSize());
  }

  public void addEast(IdentifiableWidget widget, int size, int splSize) {
    insert(widget, Direction.EAST, size, null, splSize);
  }

  public void addNorth(IdentifiableWidget widget, int size) {
    addNorth(widget, size, getSplitterSize());
  }

  public void addNorth(IdentifiableWidget widget, int size, int splSize) {
    insert(widget, Direction.NORTH, size, null, splSize);
  }

  public void addSouth(IdentifiableWidget widget, int size) {
    addSouth(widget, size, getSplitterSize());
  }

  public void addSouth(IdentifiableWidget widget, int size, int splSize) {
    insert(widget, Direction.SOUTH, size, null, splSize);
  }

  public void addWest(IdentifiableWidget widget, int size) {
    addWest(widget, size, getSplitterSize());
  }

  public void addWest(IdentifiableWidget widget, int size, int splSize) {
    insert(widget, Direction.WEST, size, null, splSize);
  }

  public void doLayout() {
    layoutChildren();
    onResize();
  }

  public IdentifiableWidget getCenter() {
    return center;
  }

  public int getCenterHeight() {
    IdentifiableWidget c = getCenter();
    if (c != null) {
      return c.asWidget().getOffsetHeight();
    }

    int y = getOffsetHeight();
    for (Widget w : getChildren()) {
      if (isVertical(w)) {
        y -= w.getOffsetHeight();
      }
    }
    return y;
  }

  public int getCenterWidth() {
    IdentifiableWidget c = getCenter();
    if (c != null) {
      return c.asWidget().getOffsetWidth();
    }

    int x = getOffsetWidth();
    for (Widget w : getChildren()) {
      if (isHorizontal(w)) {
        x -= w.getOffsetWidth();
      }
    }
    return x;
  }

  public List<ExtendedProperty> getDirectionInfo(Direction dir) {
    Assert.notNull(dir);
    List<ExtendedProperty> lst = Lists.newArrayList();

    List<Widget> children = getDirectionChildren(dir);
    int c = children.size();

    PropertyUtils.addExtended(lst, dir.toString(), "Widget Count", c);

    if (c > 0) {
      int i = 0;
      for (Widget w : children) {
        PropertyUtils.appendChildrenToExtended(lst, BeeUtils.progress(++i, c), getChildInfo(w));
      }
    }
    return lst;
  }

  public int getDirectionSize(Direction direction) {
    Assert.isTrue(validDirection(direction, false));
    int result = 0;

    for (Widget w : getChildren()) {
      if (!isSplitter(w) && direction.equals(getWidgetDirection(w))) {
        int size = getWidgetSize(w);
        if (size > 0) {
          result += size;
        }
      }
    }
    return result;
  }

  @Override
  public List<ExtendedProperty> getExtendedInfo() {
    List<ExtendedProperty> lst = Lists.newArrayList();

    PropertyUtils.addChildren(lst, NameUtils.getName(this),
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
      String name = BeeUtils.joinWords(BeeUtils.progress(++i, c), getWidgetDirection(w));
      PropertyUtils.appendChildrenToExtended(lst, name, getChildInfo(w));
    }
    return lst;
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "split";
  }

  public Direction getWidgetDirection(Widget child) {
    LayoutData data = getLayoutData(child);
    return (data == null) ? null : data.getDirection();
  }

  public int getWidgetSize(Widget child) {
    LayoutData data = getLayoutData(child);
    return (data == null) ? 0 : data.getSize();
  }

  public int getWidgetSplitterSize(Widget child) {
    Splitter splitter = getAssociatedSplitter(child);
    if (splitter == null) {
      return 0;
    } else {
      return splitter.getSize();
    }
  }

  @Override
  public void onResize() {
    if (providesResize()) {
      for (Widget child : getChildren()) {
        if (child instanceof RequiresResize) {
          ((RequiresResize) child).onResize();
        }
      }
    }
  }

  public boolean providesResize() {
    return providesResize;
  }

  @Override
  public boolean remove(Widget w) {
    Assert.notNull(w);

    if (isSplitter(w)) {
      return super.remove(w);
    }

    Splitter splitter = getAssociatedSplitter(w);

    boolean removed = super.remove(w);
    if (removed) {
      w.removeStyleName(STYLE_CHILD);
      Direction direction = getWidgetDirection(w);
      if (direction != null) {
        w.removeStyleName(STYLE_NAME + direction.getStyleSuffix());
      }

      if (w == center) {
        setCenter(null);
      }

      if (splitter != null) {
        remove(splitter);
      }
    }
    return removed;
  }

  public void setDirectionSize(Direction direction, int size, boolean doLayout) {
    Assert.isTrue(validDirection(direction, false));

    List<LayoutData> data = Lists.newArrayList();
    for (Widget child : getChildren()) {
      if (!isSplitter(child) && direction.equals(getWidgetDirection(child))) {
        LayoutData ld = getLayoutData(child);
        if (ld != null) {
          data.add(ld);
        }
      }
    }

    if (data.isEmpty()) {
      return;
    }

    if (data.size() == 1 || size <= 0) {
      for (LayoutData ld : data) {
        ld.setSize(size);
      }
    } else {
      for (LayoutData ld : data) {
        ld.setSize(size / data.size());
      }
    }

    if (doLayout) {
      doLayout();
    }
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setProvidesResize(boolean providesResize) {
    this.providesResize = providesResize;
  }

  public void setWidgetMinSize(Widget child, int minSize) {
    Splitter splitter = getAssociatedSplitter(child);
    if (splitter != null) {
      splitter.setMinSize(minSize);
    }
  }

  public void setWidgetSize(Widget widget, int size) {
    LayoutData data = getLayoutData(widget);

    if (data != null && size != data.getSize()) {
      data.setSize(size);
      doLayout();
    }
  }

  public void updateCenter(IdentifiableWidget widget) {
    Assert.notNull(widget);

    IdentifiableWidget w = getCenter();
    if (w != null) {
      remove(w.asWidget());
    }
    add(widget);
  }

  protected int getSplitterSize() {
    return splitterSize;
  }

  protected void insert(IdentifiableWidget child, Direction direction, int size,
      IdentifiableWidget before, int splSize) {

    if (Direction.CENTER.equals(direction)) {
      Assert.isTrue(getCenter() == null, "Only one CENTER widget can bee added to Split panel");
    }

    if (before == null) {
      super.add(child.asWidget(), getElement());
    } else {
      int index = getWidgetIndex(before);
      super.insert(child.asWidget(), getElement(), index, true);
    }

    LayoutData data = new LayoutData(direction, size);
    child.asWidget().setLayoutData(data);

    StyleUtils.makeAbsolute(child.asWidget());

    if (!isSplitter(child.asWidget())) {
      child.asWidget().addStyleName(STYLE_CHILD);
      child.asWidget().addStyleName(STYLE_NAME + direction.getStyleSuffix());

      if (Direction.CENTER.equals(direction)) {
        setCenter(child);
        layoutChildren();
      } else if (splSize > 0) {
        insertSplitter(direction, splSize, before);
      }
    }
  }

  protected boolean isSplitter(Widget w) {
    return w instanceof Splitter;
  }

  private Splitter getAssociatedSplitter(Widget child) {
    int index = getWidgetIndex(child);
    if (index >= 0 && index < getWidgetCount() - 1) {
      Widget splitter = getWidget(index + 1);
      if (isSplitter(splitter)) {
        return (Splitter) splitter;
      }
    }
    return null;
  }

  private List<Property> getChildInfo(Widget w) {
    List<Property> lst = Lists.newArrayList();

    Style style = w.getElement().getStyle();

    PropertyUtils.addProperties(lst,
        "Size", getWidgetSize(w),
        "Class", NameUtils.getName(w),
        "Id", DomUtils.getId(w),
        "Style Name", w.getStyleName(),
        "Left", style.getLeft(),
        "Right", style.getRight(),
        "Top", style.getTop(),
        "Bottom", style.getBottom(),
        "Width", style.getWidth(),
        "Height", style.getHeight());

    if (isSplitter(w)) {
      Splitter bspl = (Splitter) w;
      PropertyUtils.addProperties(lst,
          "Splitter Size", bspl.getSize(),
          "Reverse", bspl.isReverse(),
          "Min Size", bspl.getMinSize());
    }
    return lst;
  }

  private List<Widget> getDirectionChildren(Direction dir) {
    List<Widget> lst = Lists.newArrayList();

    for (Widget w : getChildren()) {
      if (getWidgetDirection(w) == dir) {
        lst.add(w);
      }
    }
    return lst;
  }

  private LayoutData getLayoutData(Widget child) {
    if (child == null) {
      return null;
    }

    Object data = child.getLayoutData();
    if (data instanceof LayoutData) {
      return (LayoutData) data;
    } else {
      return null;
    }
  }

  private Unit getUnit() {
    return unit;
  }

  private void insertSplitter(Direction direction, int size, IdentifiableWidget before) {
    final Splitter splitter;

    switch (direction) {
      case WEST:
        splitter = new HorizontalSplitter(size, false);
        break;
      case EAST:
        splitter = new HorizontalSplitter(size, true);
        break;
      case NORTH:
        splitter = new VerticalSplitter(size, false);
        break;
      case SOUTH:
        splitter = new VerticalSplitter(size, true);
        break;
      default:
        Assert.untouchable();
        splitter = null;
    }

    insert(splitter, direction, size, before, BeeConst.UNDEF);
  }

  private boolean isHorizontal(Widget w) {
    Direction dir = getWidgetDirection(w);
    return dir != null && dir.isHorizontal();
  }

  private boolean isVertical(Widget w) {
    Direction dir = getWidgetDirection(w);
    return dir != null && dir.isVertical();
  }

  private void layoutChildren() {
    int left = 0;
    int right = 0;

    int top = 0;
    int bottom = 0;

    for (Widget child : getChildren()) {
      LayoutData data = getLayoutData(child);
      Direction direction = data.getDirection();
      int size = data.getSize();

      Style style = child.getElement().getStyle();

      switch (direction) {
        case NORTH:
          style.setLeft(left, unit);
          style.setRight(right, unit);

          style.setTop(top, unit);
          style.setHeight(size, unit);

          top += size;
          break;

        case SOUTH:
          style.setLeft(left, unit);
          style.setRight(right, unit);

          style.setBottom(bottom, unit);
          style.setHeight(size, unit);

          bottom += size;
          break;

        case WEST:
          style.setLeft(left, unit);
          style.setWidth(size, unit);

          style.setTop(top, unit);
          style.setBottom(bottom, unit);

          left += size;
          break;

        case EAST:
          style.setRight(right, unit);
          style.setWidth(size, unit);

          style.setTop(top, unit);
          style.setBottom(bottom, unit);

          right += size;
          break;

        case CENTER:
          break;
      }
    }

    if (getCenter() != null) {
      Style style = getCenter().asWidget().getElement().getStyle();

      style.setLeft(left, unit);
      style.setRight(right, unit);

      style.setTop(top, unit);
      style.setBottom(bottom, unit);
    }
  }

  private void setCenter(IdentifiableWidget center) {
    this.center = center;
  }
}
