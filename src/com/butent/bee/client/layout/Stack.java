package com.butent.bee.client.layout;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.HasBeforeSelectionHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.animation.RafCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Contains a panel that stacks its children vertically, displaying only one at a time, with a
 * header for each child which the user can click to display.
 */

public class Stack extends ComplexPanel implements ProvidesResize, RequiresResize,
    HasBeforeSelectionHandlers<Integer>, HasSelectionHandlers<Integer>, IdentifiableWidget {

  private static class Header extends Composite implements HasClickHandlers, ProvidesResize,
      RequiresResize {

    private final int size;

    private Header(Widget child, int size) {
      super();
      this.size = size;
      
      Style style = child.getElement().getStyle();
      style.setPosition(Position.ABSOLUTE);
      style.setLeft(0, Unit.PX);
      style.setRight(0, Unit.PX);

      style.setHeight(size, Unit.PX);

      initWidget(child);
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
      return addDomHandler(handler, ClickEvent.getType());
    }

    @Override
    public void onResize() {
      if (getWidget() instanceof RequiresResize) {
        ((RequiresResize) getWidget()).onResize();
      }
    }

    @Override
    protected Widget getWidget() {
      return super.getWidget();
    }

    private int getSize() {
      return size;
    }
  }
  
  private static class Revelation extends RafCallback {
    private static final double FROM = 0.2;
    private static final double TO = 1.0;

    private Style style;

    private Revelation(double duration) {
      super(duration);
    }

    @Override
    public void onComplete() {
      super.onComplete();
      StyleUtils.clearTranform(style);
    }
    
    @Override
    public boolean run(double elapsed) {
      double factor = BeeUtils.rescale(elapsed, 0, getDuration(), FROM, TO);
      StyleUtils.setTransformScale(style, factor, factor);
      return true;
    }
    
    private void start(Style st) {
      this.style = st;
      super.start();
    }
  }

  private static final String CONTAINER_STYLE = "bee-StackContainer";
  private static final String HEADER_STYLE = "bee-StackHeader";
  private static final String CONTENT_STYLE = "bee-StackContent";

  private static final String SELECTED_STYLE = HEADER_STYLE + "-selected";
  
  private final Revelation revelation = new Revelation(200);

  private int selectedIndex = BeeConst.UNDEF;

  public Stack() {
    setElement(Document.get().createDivElement());
    
    setStyleName(CONTAINER_STYLE);
    DomUtils.createId(this, getIdPrefix());
  }

  @Override
  public void add(Widget w) {
    Assert.unsupported("Single-argument add() is not supported for Stack");
  }

  public void add(Widget widget, String header, boolean asHtml, int headerSize) {
    insert(widget, header, asHtml, headerSize, getStackSize());
  }

  public void add(Widget widget, Widget header, int headerSize) {
    insert(widget, header, headerSize, getStackSize());
  }

  @Override
  public HandlerRegistration addBeforeSelectionHandler(BeforeSelectionHandler<Integer> handler) {
    return addHandler(handler, BeforeSelectionEvent.getType());
  }

  @Override
  public HandlerRegistration addSelectionHandler(SelectionHandler<Integer> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

  @Override
  public void clear() {
    setSelectedIndex(BeeConst.UNDEF);
    while (!isEmpty()) {
      remove(0);
    }
  }

  public void doLayout() {
    if (isAttached()) {
      layoutChildren();
      if (getSelectedIndex() >= 0) {
        onResize();
      }
    }
  }

  public int getContentIndex(Widget content) {
    int index = super.getWidgetIndex(content);
    return (index > 0) ? (index - 1) / 2 : BeeConst.UNDEF;
  }

  public Widget getContentWidget(int index) {
    checkIndex(index);
    return super.getWidget(index * 2 + 1);
  }

  public Widget getHeaderWidget(int index) {
    checkIndex(index);
    return getHeader(index).getWidget();
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "stack";
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public int getStackSize() {
    return super.getWidgetCount() / 2;
  }

  public Widget getVisibleWidget() {
    if (BeeConst.isUndef(getSelectedIndex())) {
      return null;
    } else {
      return getContentWidget(getSelectedIndex());
    }
  }

  public void insert(Widget child, String text, boolean asHtml, int headerSize, int before) {
    Html contents = new Html();
    if (asHtml) {
      contents.setHTML(text);
    } else {
      contents.setText(text);
    }
    insert(child, contents, headerSize, before);
  }

  public void insert(Widget child, Widget header, int headerSize, int before) {
    insert(child, new Header(header, headerSize), before);
  }

  public boolean isEmpty() {
    return getStackSize() == 0;
  }

  @Override
  public void onResize() {
    for (Widget child : getChildren()) {
      if (child instanceof RequiresResize && child.isVisible()) {
        ((RequiresResize) child).onResize();
      }
    }
  }

  @Override
  public boolean remove(int index) {
    return remove(getContentWidget(index));
  }

  @Override
  public boolean remove(Widget child) {
    if (child instanceof Header) {
      return super.remove(child);
    }

    int index = getContentIndex(child);
    if (BeeConst.isUndef(index)) {
      return false;
    }

    boolean removed = super.remove(getHeader(index));
    if (removed) {
      removed = super.remove(child);
    }

    if (removed) {
      if (getSelectedIndex() == index) {
        setSelectedIndex(BeeConst.UNDEF);
      } else {
        if (index < getSelectedIndex()) {
          setSelectedIndex(getSelectedIndex() - 1);
        }
      }
      doLayout();
    }
    return removed;
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void showWidget(int index) {
    showWidget(index, true);
  }

  public void showWidget(int index, boolean fireEvents) {
    checkIndex(index);
    if (index == getSelectedIndex()) {
      return;
    }

    if (fireEvents) {
      BeforeSelectionEvent<Integer> event = BeforeSelectionEvent.fire(this, index);
      if ((event != null) && event.isCanceled()) {
        return;
      }
    }
    
    if (isIndex(getSelectedIndex())) {
      hideContent(getSelectedIndex());
    }

    setSelectedIndex(index);
    doLayout();

    if (fireEvents) {
      SelectionEvent.fire(this, index);
    }
  }

  public void showWidget(Widget child) {
    showWidget(getContentIndex(child));
  }

  protected State onHeaderClick(Widget child) {
    int index = getContentIndex(child);
    if (!isIndex(index)) {
      return null;
    }
    
    if (index == getSelectedIndex()) {
      hideContent(index);
      setSelectedIndex(BeeConst.UNDEF);
      layoutChildren();
      return State.CLOSED;

    } else {
      showWidget(index);
      return State.OPEN;
    }
  }

  @Override
  protected void onLoad() {
    layoutChildren();
    super.onLoad();
  }

  private void checkIndex(int index) {
    Assert.betweenExclusive(index, 0, getStackSize(), "Index out of bounds");
  }

  private Header getHeader(int index) {
    return (Header) super.getWidget(index * 2);
  }
  
  private void hideContent(int index) {
    getContentWidget(index).setVisible(false);
  }

  private void insert(final Widget child, Header header, int before) {
    Assert.betweenInclusive(before, 0, getStackSize(), "before index out of bounds");
    
    child.setVisible(false);

    header.addStyleName(HEADER_STYLE);
    child.addStyleName(CONTENT_STYLE);
    
    Style style = child.getElement().getStyle();
    style.setPosition(Position.ABSOLUTE);
    style.setLeft(0, Unit.PX);
    style.setRight(0, Unit.PX);

    header.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Stack.this.onHeaderClick(child);
      }
    });
    
    super.insert(header, getElement(), before * 2, true);
    super.insert(child, getElement(), before * 2 + 1, true);
    
    if (before >= 0 && before <= getSelectedIndex()) {
      setSelectedIndex(getSelectedIndex() + 1);
    }
    doLayout();
  }

  private boolean isIndex(int index) {
    return index >= 0 && index < getStackSize();
  }

  private void layoutChildren() {
    if (isEmpty()) {
      return;
    }

    int top = 0;
    int bottom = 0;

    if (isIndex(getSelectedIndex())) {
      for (int i = 0; i <= getSelectedIndex(); i++) {
        Header header = getHeader(i);
        Style style = header.getElement().getStyle();

        style.setTop(top, Unit.PX);
        style.clearBottom();

        top += header.getSize();
      }

      for (int i = getStackSize() - 1; i > getSelectedIndex(); i--) {
        Header header = getHeader(i);
        Style style = header.getElement().getStyle();

        style.clearTop();
        style.setBottom(bottom, Unit.PX);

        bottom += header.getSize();
      }

      Widget widget = getVisibleWidget();
      Style style = widget.getElement().getStyle();

      style.setTop(top, Unit.PX);
      style.setBottom(bottom, Unit.PX);
      
      StyleUtils.setTransformScale(style, Revelation.FROM, Revelation.FROM);
      widget.setVisible(true);
      
      revelation.start(style);

    } else {
      for (int i = 0; i < getStackSize(); i++) {
        Header header = getHeader(i);
        Style style = header.getElement().getStyle();

        style.setTop(top, Unit.PX);
        style.clearBottom();

        top += header.getSize();
      }
    }
  }

  private void setSelectedIndex(int selectedIndex) {
    if (this.selectedIndex == selectedIndex) {
      return;
    }

    if (isIndex(this.selectedIndex)) {
      getHeaderWidget(this.selectedIndex).removeStyleName(SELECTED_STYLE);
    }

    this.selectedIndex = selectedIndex;

    if (isIndex(selectedIndex)) {
      getHeaderWidget(selectedIndex).addStyleName(SELECTED_STYLE);
    }
  }
}
