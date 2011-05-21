package com.butent.bee.client.view;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.view.grid.CellGridImpl;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.navigation.PagerView;
import com.butent.bee.client.view.navigation.ScrollPager;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;

import java.util.Collection;
import java.util.List;

/**
 * Implements design content for a grid container component.
 */

public class GridContainerImpl extends Split implements GridContainerView, HasNavigation, HasSearch {

  public static int minPagingRows = 20;
  public static int minSearchRows = 2;

  public static int defaultPageSize = 15;

  private Presenter viewPresenter = null;

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
  
  public void bind() {
    if (hasHeader()) {
      getContent().getGrid().addLoadingStateChangeHandler(getHeader());
    }
    if (hasFooter()) {
      getContent().getGrid().addSelectionCountChangeHandler(getFooter());
    }
  }

  public void create(String caption, List<BeeColumn> dataColumns, int rowCount, BeeRowSet rowSet) {
    hasPaging = rowCount >= minPagingRows;
    hasSearch = rowCount >= minSearchRows;

    int pageSize = hasPaging ? defaultPageSize : rowCount;

    DataHeaderView header = new DataHeaderImpl();
    header.create(caption);

    GridView content = new CellGridImpl();
    content.create(dataColumns, rowCount, rowSet);

    DataFooterView footer;
    ScrollPager scroller;

    if (hasPaging || hasSearch) {
      footer = new DataFooterImpl();
      footer.create(rowCount, pageSize, hasPaging, hasSearch);
    } else {
      footer = null;
    }

    if (hasPaging) {
      scroller = new ScrollPager();
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
  
  @Override
  public void createId() {
    DomUtils.createId(this, "grid-container");
  }

  public GridView getContent() {
    if (getCenter() == null) {
      return null;
    }
    return (GridView) getCenter();
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

  public Collection<PagerView> getPagers() {
    if (hasPaging) {
      return ViewHelper.getPagers(this);
    } else {
      return null;
    }
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

  public Collection<SearchView> getSearchers() {
    if (hasSearch) {
      return ViewHelper.getSearchers(this);
    } else {
      return null;
    }
  }

  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  public String getWidgetId() {
    return getId();
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

      HasDataTable display = getContent().getGrid();
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
    if (isAttached()) {
      super.onResize();
      adapt(false);
    }
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

  public void setViewPresenter(Presenter viewPresenter) {
    this.viewPresenter = viewPresenter;
    for (Widget child : getChildren()) {
      if (child instanceof View) {
        ((View) child).setViewPresenter(viewPresenter);
      }
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      public void execute() {
        adapt(true);
      }
    });
  }

  @Override
  protected void onUnload() {
    if (getViewPresenter() != null) {
      getViewPresenter().onViewUnload();
    }
    super.onUnload();
  }

  private void adapt(boolean init) {
    GridView content = getContent();
    Assert.notNull(content);

    if (hasPaging) {
      int w = getElement().getClientWidth();
      int h = getElement().getClientHeight();

      if (w <= 0) {
        w = DomUtils.getParentClientWidth(this);
      }
      if (h <= 0) {
        h = DomUtils.getParentClientHeight(this);
      }

      int pageSize = estimatePageSize(getContent(), w, h);
      if (pageSize > 0 && (init || pageSize != getPageSize(content))) {
        updatePageSize(content, pageSize, init);
      }

      if (init) {
        Collection<PagerView> pagers = getPagers();
        if (pagers != null) {
          for (PagerView pager : pagers) {
            pager.start(content.getGrid());
          }
        }
      }
    } else if (init) {
      int rc = content.getGrid().getRowCount();
      int pageSize = (rc > 0) ? rc : 10;
      getContent().updatePageSize(pageSize, init);
    }
  }

  private int estimatePageSize(GridView content, int containerWidth, int containerHeight) {
    if (content != null && containerHeight > 0) {
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
    return BeeConst.SIZE_UNKNOWN;
  }

  private int getPageSize(GridView content) {
    if (content == null) {
      return BeeConst.SIZE_UNKNOWN;
    } else {
      return content.getGrid().getVisibleRange().getLength();
    }
  }

  private void updatePageSize(GridView content, int pageSize, boolean init) {
    if (content != null && pageSize > 0 && hasPaging) {
      content.updatePageSize(pageSize, init);
    }
  }
}
