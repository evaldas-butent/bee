package com.butent.bee.client.view;

import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.data.ScrollPager;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;

import java.util.List;

public class GridContainerImpl extends Split implements GridContainerView {
  public static int minPagingRows = 20;
  public static int minSearchRows = 2;
  
  public static int defaultPageSize = 15;

  private Presenter presenter = null;

  private Direction footerDirection = null;
  private Direction headerDirection = null;
  private Direction scrollerDirection = null;

  private int headerHeight = 22;
  private int footerHeight = 32;

  private int scrollerWidth = DomUtils.getScrollbarWidth() + 1;

  private boolean hasPaging = false;
  private boolean hasSearch = false;

  public GridContainerImpl() {
    this(-1);
  }

  public GridContainerImpl(int splitterSize) {
    super(splitterSize);
  }

  public GridContainerImpl(String style) {
    this(style, -1);
  }

  public GridContainerImpl(String style, int splitterSize) {
    super(style, splitterSize);
  }

  public void create(String caption, List<BeeColumn> dataColumns, int rowCount) {
    hasPaging = rowCount >= minPagingRows;
    hasSearch = rowCount >= minSearchRows;
    
    int pageSize = hasPaging ? defaultPageSize : rowCount;

    DataHeaderView header = new DataHeaderImpl();
    header.create(caption);

    GridContentView content = new GridContentImpl(pageSize);
    content.create(dataColumns, rowCount);

    DataFooterView footer;
    ScrollPager scroller;

    if (hasPaging || hasSearch) {
      footer = new DataFooterImpl();
      footer.create(content, rowCount, pageSize, hasPaging, hasSearch);
    } else {
      footer = null;
    }

    if (hasPaging) {
      scroller = new ScrollPager();
      scroller.setDisplay(content);
    } else {
      scroller = null;
    }

    addNorth(header.asWidget(), headerHeight);
    headerDirection = Direction.NORTH;

    if (footer != null) {
      addSouth(footer.asWidget(), footerHeight);
      footerDirection = Direction.SOUTH;
    }

    if (scroller != null) {
      addEast(scroller, scrollerWidth);
      scrollerDirection = Direction.EAST;
      add(content.asWidget(), ScrollBars.HORIZONTAL);
      sinkEvents(Event.ONMOUSEWHEEL);
    } else {
      add(content.asWidget(), ScrollBars.BOTH);
    }
  }

  public int estimatePageSize(int containerWidth, int containerHeight) {
    if (containerHeight > 0) {
      GridContentView content = getContent();
      if (content != null) {
        int w = containerWidth;
        int h = containerHeight;

        if (hasHeader()) {
          h -= getHeaderHeight();
        }
        if (hasFooter()) {
          h -= getFooterHeight();
        }
        if (hasScroller()) {
          w -= getScrollerWidth();
        }
        return content.estimatePageSize(w, h);
      }
    }
    return BeeConst.SIZE_UNKNOWN;
  }

  public GridContentView getContent() {
    if (getCenter() == null) {
      return null;
    }
    return (GridContentView) getCenter();
  }

  public DataFooterView getFooter() {
    if (footerDirection == null) {
      return null;
    }
    for (Widget widget : getDirectionChildren(footerDirection)) {
      if (widget instanceof DataFooterView) {
        return (DataFooterView) widget;
      }
    }
    return null;
  }

  public int getFooterHeight() {
    return footerHeight;
  }

  public DataHeaderView getHeader() {
    if (headerDirection == null) {
      return null;
    }
    for (Widget widget : getDirectionChildren(headerDirection)) {
      if (widget instanceof DataHeaderView) {
        return (DataHeaderView) widget;
      }
    }
    return null;
  }

  public int getHeaderHeight() {
    return headerHeight;
  }

  public ScrollPager getScroller() {
    if (scrollerDirection == null) {
      return null;
    }
    for (Widget widget : getDirectionChildren(scrollerDirection)) {
      if (widget instanceof ScrollPager) {
        return (ScrollPager) widget;
      }
    }
    return null;
  }

  public int getScrollerWidth() {
    return scrollerWidth;
  }

  public SearchView getSearchView() {
    DataFooterView footer = getFooter();
    if (footer == null) {
      return null;
    }
    return footer.getSearchView();
  }

  public Presenter getViewPresenter() {
    return presenter;
  }

  public boolean hasFooter() {
    return footerDirection != null;
  }

  public boolean hasHeader() {
    return headerDirection != null;
  }

  public boolean hasScroller() {
    return scrollerDirection != null;
  }

  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);

    if (event.getTypeInt() == Event.ONMOUSEWHEEL) {
      int y = event.getMouseWheelVelocityY();

      GridContentView display = getContent();
      ScrollPager scroller = getScroller();

      if (y == 0 || display == null || scroller == null) {
        return;
      }

      Element elem = scroller.getElement();
      EventTarget target = event.getEventTarget();
      if (target != null && elem.isOrHasChild(Node.as(target))) {
        return;
      }

      EventUtils.eatEvent(event);

      int rc = display.getRowCount();
      int start = display.getVisibleRange().getStart();
      int length = display.getVisibleRange().getLength();

      if (length > 0 && rc > length) {
        int p = -1;
        if (y > 0 && start + length < rc) {
          p = start + 1;
        } else if (y < 0 && start > 0) {
          p = start - 1;
        }

        if (p >= 0) {
          display.setVisibleRange(p, length);
        }
      }
    }
  }

  @Override
  public void onResize() {
    if (hasPaging) {
      int ps = estimatePageSize(getElement().getClientWidth(), getElement().getClientHeight());
      if (ps > 0 && ps != getPageSize()) {
        updatePageSize(ps);
      }
    }
    super.onResize();
  }

  public void setFooterHeight(int footerHeight) {
    this.footerHeight = footerHeight;
  }

  public void setHeaderHeight(int headerHeight) {
    this.headerHeight = headerHeight;
  }

  public void setScrollerWidth(int scrollerWidth) {
    this.scrollerWidth = scrollerWidth;
  }

  public void setViewPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  public void updatePageSize(int pageSize) {
    GridContentView content = getContent();
    if (content != null && pageSize > 0 && hasPaging) {
      content.updatePageSize(pageSize);
    }
  }

  private int getPageSize() {
    GridContentView content = getContent();
    if (content == null) {
      return BeeConst.SIZE_UNKNOWN;
    } else {
      return content.getVisibleRange().getLength();
    }
  }
}
