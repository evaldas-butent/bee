package com.butent.bee.client.view.navigation;

import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasRows;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.layout.Scroll;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Enables to use scroll function in pager user interface elements.
 */

public class ScrollPager extends AbstractPagerImpl implements RequiresResize {

  public static int maxHeight = 10000;

  private static final int UNKNOWN = -1;

  private int lastPos = UNKNOWN;
  private long lastHeight = UNKNOWN;

  private boolean isScrolling = false;
  private boolean isUpdating = false;

  public ScrollPager() {
    Widget widget = new Html();
    DomUtils.setWidth(widget, 0);
    DomUtils.setHeight(widget, 0);

    Scroll scroll = new Scroll(widget);
    initWidget(scroll);

    StyleUtils.hideScroll(scroll, ScrollBars.HORIZONTAL);
    StyleUtils.alwaysScroll(scroll, ScrollBars.VERTICAL);

    scroll.addScrollHandler(new ScrollHandler() {
      public void onScroll(ScrollEvent event) {
        if (isUpdating) {
          isUpdating = false;
          return;
        }
        if (!isEnabled()) {
          return;
        }

        int pos = getPosition();
        int maxPos = getMaxPosition();
        int height = getWidgetHeight();
        if (pos < 0 || pos == lastPos || height >= maxPos || pos > maxPos - height) {
          return;
        }

        int pageSize = getPageSize();
        int rowCount = getRowCount();
        if (pageSize <= 0 || pageSize >= rowCount) {
          return;
        }

        int start = pos * (rowCount - pageSize) / (maxPos - height);
        start = BeeUtils.limit(start, 0, rowCount - pageSize);

        if (start != getPageStart()) {
          isScrolling = true;
          setPageStart(start);
        }
        lastPos = pos;
      }
    });
  }

  @Override
  public void onResize() {
    updateHeight();
  }

  @Override
  protected void onRangeOrRowCountChanged() {
    if (isScrolling) {
      isScrolling = false;
      return;
    }
    isUpdating = true;

    updateHeight();
    updatePosition();
  }

  private long calculateHeight(int pageSize, int rowCount, int widgetHeight) {
    if (pageSize <= 0 || rowCount < pageSize || widgetHeight <= 0) {
      return UNKNOWN;
    }
    return (long) widgetHeight * rowCount / pageSize;
  }

  private Widget getInnerWidget() {
    Scroll outer = getOuterWidget();
    if (outer == null) {
      return null;
    }
    return outer.getWidget();
  }
  
  private int getMaxPosition() {
    if (lastHeight < maxHeight) {
      return (int) lastHeight;
    } else {
      return maxHeight;
    }
  }

  private Scroll getOuterWidget() {
    Widget outer = getWidget();
    if (outer == null) {
      return null;
    }
    return (Scroll) outer;
  }

  private int getPosition() {
    Scroll outer = getOuterWidget();
    if (outer == null) {
      return UNKNOWN;
    }
    return outer.getVerticalScrollPosition();
  }

  private int getRowCount() {
    HasRows display = getDisplay();
    if (display == null) {
      return UNKNOWN;
    } else {
      return display.getRowCount();
    }
  }

  private int getWidgetHeight() {
    return getElement().getClientHeight();
  }
  
  private void setPosition(int position) {
    Scroll outer = getOuterWidget();
    if (outer != null && position >= 0) {
      outer.setVerticalScrollPosition(position);
    }
  }

  private void updateHeight() {
    long h = calculateHeight(getPageSize(), getRowCount(), getWidgetHeight());
    if (h >= 0 && h != lastHeight) {
      lastHeight = h;
      int z = (h < maxHeight) ? (int) h : maxHeight;
      DomUtils.setHeight(getInnerWidget(), z);
    }
  }

  private void updatePosition() {
    int pageStart = getPageStart();
    int pageSize = getPageSize();
    int rowCount = getRowCount();

    int maxPos = getMaxPosition();
    int height = getWidgetHeight();
    
    int pos = 0;
    if (rowCount > 0 && pageSize > 0 && rowCount > pageSize) {    
      pos = pageStart * (maxPos - height) / (rowCount - pageSize);
    }

    if (pos >= 0 && pos <= maxPos - height) {
      setPosition(pos);
      lastPos = pos;
    }
  }
}
