package com.butent.bee.client.layout;

import com.google.common.collect.Lists;
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
import com.google.gwt.layout.client.Layout.AnimationCallback;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.AnimatedLayout;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasId;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Contains a panel that stacks its children vertically, displaying only one at a time, with a
 * header for each child which the user can click to display.
 */

public class Stack extends ResizeComposite implements HasWidgets,
    ProvidesResize, IndexedPanel.ForIsWidget, AnimatedLayout,
    HasBeforeSelectionHandlers<Integer>, HasSelectionHandlers<Integer>, HasId {

  private class Header extends Composite implements HasClickHandlers {
    private Header(Widget child) {
      initWidget(child);
    }

    public HandlerRegistration addClickHandler(ClickHandler handler) {
      return this.addDomHandler(handler, ClickEvent.getType());
    }

    @Override
    protected Widget getWidget() {
      return super.getWidget();
    }
  }

  private static class LayoutData {
    private final double headerSize;
    private final Header header;
    private final Widget widget;

    private LayoutData(Widget widget, Header header, double headerSize) {
      this.widget = widget;
      this.header = header;
      this.headerSize = headerSize;
    }
  }

  private static final String CONTAINER_STYLE = "bee-StackContainer";
  private static final String HEADER_STYLE = "bee-StackHeader";
  private static final String CONTENT_STYLE = "bee-StackContent";

  private static final int ANIMATION_TIME = 250;

  private int animationDuration = ANIMATION_TIME;

  private final BeeLayoutPanel layoutPanel;
  
  private final Unit unit;

  private final List<LayoutData> layoutData = Lists.newArrayList();

  private int selectedIndex = BeeConst.UNDEF;

  public Stack(Unit unit) {
    this.unit = unit;
    this.layoutPanel = new BeeLayoutPanel();
    initWidget(layoutPanel);
    
    setStyleName(CONTAINER_STYLE);
    DomUtils.createId(this, getIdPrefix());
  }

  public void add(IsWidget widget, IsWidget header, double headerSize) {
    this.add(widget.asWidget(), header.asWidget(), headerSize);
  }

  public void add(IsWidget widget, String header, boolean asHtml, double headerSize) {
    this.add(widget.asWidget(), header, asHtml, headerSize);
  }
  
  public void add(Widget w) {
    Assert.unsupported("Single-argument add() is not supported for Stack");
  }

  public void add(Widget widget, SafeHtml header, double headerSize) {
    add(widget, header.asString(), true, headerSize);
  }

  public void add(Widget widget, String header, boolean asHtml, double headerSize) {
    insert(widget, header, asHtml, headerSize, getWidgetCount());
  }

  public void add(Widget widget, String header, double headerSize) {
    insert(widget, header, headerSize, getWidgetCount());
  }

  public void add(Widget widget, Widget header, double headerSize) {
    insert(widget, header, headerSize, getWidgetCount());
  }

  public HandlerRegistration addBeforeSelectionHandler(BeforeSelectionHandler<Integer> handler) {
    return addHandler(handler, BeforeSelectionEvent.getType());
  }

  public HandlerRegistration addSelectionHandler(SelectionHandler<Integer> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

  public void animate(int duration) {
    animate(duration, null);
  }

  public void animate(int duration, AnimationCallback callback) {
    if (layoutData.isEmpty()) {
      if (callback != null) {
        callback.onAnimationComplete();
      }
      return;
    }

    double top = 0;
    double bottom = 0;

    int i = 0;
    for (; i < layoutData.size(); i++) {
      LayoutData data = layoutData.get(i);
      layoutPanel.setWidgetTopHeight(data.header, top, unit, data.headerSize, unit);

      top += data.headerSize;
      layoutPanel.setWidgetTopHeight(data.widget, top, unit, 0, unit);

      if (i == getVisibleIndex()) {
        break;
      }
    }

    for (int j = layoutData.size() - 1; j > i; j--) {
      LayoutData data = layoutData.get(j);
      layoutPanel.setWidgetBottomHeight(data.header, bottom, unit, data.headerSize, unit);
      layoutPanel.setWidgetBottomHeight(data.widget, bottom, unit, 0, unit);

      bottom += data.headerSize;
    }

    LayoutData data = layoutData.get(getVisibleIndex());
    layoutPanel.setWidgetTopBottom(data.widget, top, unit, bottom, unit);

    layoutPanel.animate(duration, callback);
  }

  public void clear() {
    layoutPanel.clear();
    layoutData.clear();
    
    setSelectedIndex(BeeConst.UNDEF);
  }

  public void forceLayout() {
    layoutPanel.forceLayout();
  }

  public int getAnimationDuration() {
    return animationDuration;
  }

  public Widget getHeaderWidget(int index) {
    checkIndex(index);
    return layoutData.get(index).header.getWidget();
  }

  public Widget getHeaderWidget(Widget child) {
    checkChild(child);
    return getHeaderWidget(getWidgetIndex(child));
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "stack";
  }

  public int getVisibleIndex() {
    return selectedIndex;
  }

  public Widget getVisibleWidget() {
    if (BeeConst.isUndef(getVisibleIndex())) {
      return null;
    } else {
      return getWidget(getVisibleIndex());
    }
  }

  public Widget getWidget(int index) {
    return layoutPanel.getWidget(index * 2 + 1);
  }

  public int getWidgetCount() {
    return layoutPanel.getWidgetCount() / 2;
  }

  public int getWidgetIndex(IsWidget child) {
    return getWidgetIndex(asWidgetOrNull(child));
  }

  public int getWidgetIndex(Widget child) {
    int index = layoutPanel.getWidgetIndex(child);
    return BeeConst.isUndef(index) ? index : (index - 1) / 2;
  }

  public void insert(Widget child, SafeHtml html, double headerSize, int beforeIndex) {
    insert(child, html.asString(), true, headerSize, beforeIndex);
  }

  public void insert(Widget child, String text, boolean asHtml, double headerSize, int beforeIndex) {
    Html contents = new Html();
    if (asHtml) {
      contents.setHTML(text);
    } else {
      contents.setText(text);
    }
    insert(child, contents, headerSize, beforeIndex);
  }

  public void insert(Widget child, String text, double headerSize, int beforeIndex) {
    insert(child, text, false, headerSize, beforeIndex);
  }

  public void insert(Widget child, Widget header, double headerSize, int beforeIndex) {
    insert(child, new Header(header), headerSize, beforeIndex);
  }

  public Iterator<Widget> iterator() {
    return new Iterator<Widget>() {
      int i = 0;
      int last = -1;

      public boolean hasNext() {
        return i < layoutData.size();
      }

      public Widget next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return layoutData.get(last = i++).widget;
      }

      public void remove() {
        if (last < 0) {
          throw new IllegalStateException();
        }

        Stack.this.remove(layoutData.get(last).widget);
        i = last;
        last = -1;
      }
    };
  }

  @Override
  public void onResize() {
    layoutPanel.onResize();
  }

  public boolean remove(int index) {
    return remove(getWidget(index));
  }

  public boolean remove(Widget child) {
    if (child.getParent() != layoutPanel) {
      return false;
    }

    for (int i = 0; i < layoutData.size(); ++i) {
      LayoutData data = layoutData.get(i);
      if (data.widget == child) {
        layoutPanel.remove(data.header);
        layoutPanel.remove(data.widget);

        data.header.removeStyleName(HEADER_STYLE);
        data.widget.removeStyleName(CONTENT_STYLE);

        layoutData.remove(i);

        if (getVisibleIndex() == i) {
          setSelectedIndex(BeeConst.UNDEF);
          if (!layoutData.isEmpty()) {
            showWidget(layoutData.get(0).widget);
          }
        } else {
          if (i <= getVisibleIndex()) {
            setSelectedIndex(getVisibleIndex() - 1);
          }
          animate(getAnimationDuration());
        }
        return true;
      }
    }

    return false;
  }

  public void setAnimationDuration(int duration) {
    this.animationDuration = duration;
  }

  public void setHeaderHml(int index, SafeHtml html) {
    setHeaderHtml(index, html.asString());
  }

  public void setHeaderHtml(int index, String html) {
    checkIndex(index);
    LayoutData data = layoutData.get(index);

    Widget headerWidget = data.header.getWidget();
    Assert.isTrue(headerWidget instanceof HasHTML, "Header widget does not implement HasHTML");
    ((HasHTML) headerWidget).setHTML(html);
  }

  public void setHeaderText(int index, String text) {
    checkIndex(index);
    LayoutData data = layoutData.get(index);

    Widget headerWidget = data.header.getWidget();
    Assert.isTrue(headerWidget instanceof HasText, "Header widget does not implement HasText");
    ((HasText) headerWidget).setText(text);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void showWidget(int index) {
    showWidget(index, true);
  }

  public void showWidget(int index, boolean fireEvents) {
    checkIndex(index);
    showWidget(index, getAnimationDuration(), fireEvents);
  }

  public void showWidget(Widget child) {
    showWidget(getWidgetIndex(child));
  }

  public void showWidget(Widget child, boolean fireEvents) {
    showWidget(getWidgetIndex(child), getAnimationDuration(), fireEvents);
  }

  @Override
  protected void onLoad() {
    animate(0);
  }

  private void checkChild(Widget child) {
    Assert.isTrue(layoutPanel.getChildren().contains(child));
  }

  private void checkIndex(int index) {
    Assert.betweenExclusive(index, 0, getWidgetCount(), "Index out of bounds");
  }

  private void insert(final Widget child, Header header, double headerSize, int beforeIndex) {
    Assert.betweenInclusive(beforeIndex, 0, getWidgetCount(), "beforeIndex out of bounds");
    
    int index = beforeIndex;
    int oldIdx = getWidgetIndex(child);
    if (!BeeConst.isUndef(oldIdx)) {
      remove(child);
      if (oldIdx < index) {
        index--;
      }
    }

    int widgetIndex = index * 2;
    layoutPanel.insert(child, widgetIndex);
    layoutPanel.insert(header, widgetIndex);

    layoutPanel.setWidgetLeftRight(header, 0, Unit.PX, 0, Unit.PX);
    layoutPanel.setWidgetLeftRight(child, 0, Unit.PX, 0, Unit.PX);

    LayoutData data = new LayoutData(child, header, headerSize);
    layoutData.add(index, data);

    header.addStyleName(HEADER_STYLE);
    child.addStyleName(CONTENT_STYLE);

    header.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showWidget(child);
      }
    });

    if (BeeConst.isUndef(getVisibleIndex())) {
      showWidget(0);
    } else if (index <= getVisibleIndex()) {
      setSelectedIndex(getVisibleIndex() + 1);
    }

    if (isAttached()) {
      animate(getAnimationDuration());
    }
  }

  private void setSelectedIndex(int selectedIndex) {
    this.selectedIndex = selectedIndex;
  }

  private void showWidget(int index, final int duration, boolean fireEvents) {
    checkIndex(index);
    if (index == getVisibleIndex()) {
      return;
    }

    if (fireEvents) {
      BeforeSelectionEvent<Integer> event = BeforeSelectionEvent.fire(this, index);
      if ((event != null) && event.isCanceled()) {
        return;
      }
    }

    setSelectedIndex(index);

    if (isAttached()) {
      animate(duration);
    }

    if (fireEvents) {
      SelectionEvent.fire(this, index);
    }
  }
}
