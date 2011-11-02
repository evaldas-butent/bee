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
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.view.add.AddEndEvent;
import com.butent.bee.client.view.add.AddStartEvent;
import com.butent.bee.client.view.edit.EditFormEvent;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.CellGridImpl;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.navigation.PagerView;
import com.butent.bee.client.view.navigation.ScrollPager;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.event.ActiveRowChangeEvent;
import com.butent.bee.shared.data.event.DataRequestEvent;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

/**
 * Implements design content for a grid container component.
 */

public class GridContainerImpl extends Split implements GridContainerView, HasNavigation,
    HasSearch, ActiveRowChangeEvent.Handler, AddStartEvent.Handler, AddEndEvent.Handler,
    EditFormEvent.Handler {

  public static String newRowCaption = "New Row";

  private Presenter viewPresenter = null;

  private Direction footerDirection = null;
  private Direction headerDirection = null;
  private Direction scrollerDirection = null;

  private int headerHeight = 22;
  private int footerHeight = 32;

  private int scrollerWidth = DomUtils.getScrollbarWidth() + 1;

  private boolean hasPaging = false;
  private boolean hasSearch = false;

  private Evaluator rowMessage = null;
  
  private boolean adding = false;
  private boolean enabled = true;
  
  private String currentCaption = null;
  
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

    if (getRowMessage() != null) {
      getContent().getGrid().addActiveRowChangeHandler(this);
    }
    
    getContent().addAddStartHandler(this);
    getContent().addAddEndHandler(this);
    
    getContent().addEditFormHandler(this);
  }

  public void create(GridDescription gridDescription, List<BeeColumn> dataColumns, int rowCount,
      BeeRowSet rowSet, GridCallback gridCallback, Collection<UiOption> options) {

    int minRows = BeeUtils.unbox(gridDescription.getPagingThreshold());
    setHasPaging(UiOption.hasPaging(options) && rowCount >= minRows);

    minRows = BeeUtils.unbox(gridDescription.getSearchThreshold());
    setHasSearch(UiOption.hasSearch(options) && rowCount >= minRows);

    boolean readOnly = BeeUtils.isTrue(gridDescription.isReadOnly());

    DataHeaderView header = new DataHeaderImpl();
    header.create(gridDescription.getCaption(), true, readOnly, options);

    GridView content = new CellGridImpl();
    content.create(dataColumns, rowCount, rowSet, gridDescription, gridCallback, hasSearch());

    DataFooterView footer;
    ScrollPager scroller;

    if (hasPaging() || hasSearch()) {
      footer = new DataFooterImpl();
      footer.create(rowCount, hasPaging(), true, hasSearch());
    } else {
      footer = null;
    }

    if (hasPaging()) {
      scroller = new ScrollPager();
    } else {
      scroller = null;
    }

    addNorth(header.asWidget(), getHeaderHeight());
    setHeaderDirection(Direction.NORTH);

    if (footer != null) {
      addSouth(footer.asWidget(), getFooterHeight());
      setFooterDirection(Direction.SOUTH);
    }

    if (scroller != null) {
      addEast(scroller, getScrollerWidth());
      setScrollerDirection(Direction.EAST);
      add(content.asWidget(), ScrollBars.HORIZONTAL);
      sinkEvents(Event.ONMOUSEWHEEL);
    } else {
      add(content.asWidget(), ScrollBars.BOTH);
    }

    if (gridDescription.getRowMessage() != null) {
      setRowMessage(Evaluator.create(gridDescription.getRowMessage(), null, dataColumns));
    }
  }

  public GridView getContent() {
    if (getCenter() == null) {
      return null;
    }
    return (GridView) getCenter();
  }

  public DataFooterView getFooter() {
    if (getFooterDirection() == null) {
      return null;
    }
    for (Widget widget : getDirectionChildren(getFooterDirection())) {
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
    if (getHeaderDirection() == null) {
      return null;
    }
    for (Widget widget : getDirectionChildren(getHeaderDirection())) {
      if (widget instanceof DataHeaderView) {
        return (DataHeaderView) widget;
      }
    }
    return null;
  }

  public int getHeaderHeight() {
    return headerHeight;
  }

  @Override
  public String getIdPrefix() {
    return "grid-container";
  }

  public Collection<PagerView> getPagers() {
    if (hasPaging()) {
      return ViewHelper.getPagers(this);
    } else {
      return null;
    }
  }

  public ScrollPager getScroller() {
    if (getScrollerDirection() == null) {
      return null;
    }
    for (Widget widget : getDirectionChildren(getScrollerDirection())) {
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
    if (hasSearch()) {
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
    return getFooterDirection() != null;
  }

  public boolean hasHeader() {
    return getHeaderDirection() != null;
  }

  public boolean hasScroller() {
    return getScrollerDirection() != null;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void onActiveRowChange(ActiveRowChangeEvent event) {
    if (event == null || event.getRowValue() == null || getRowMessage() == null) {
      return;
    }
    getRowMessage().update(event.getRowValue());
    String message = getRowMessage().evaluate();

    if (!BeeUtils.isEmpty(message)) {
      getHeader().setCaption(message);
    }
  }
  
  public void onAddEnd(AddEndEvent event) {
    if (hasHeader()) {
      getHeader().setCaption(getCurrentCaption());
    }
    
    showHeaderFooterScroller(true);
    setAdding(false);
  }

  public void onAddStart(AddStartEvent event) {
    setAdding(true);
    showHeaderFooterScroller(false);
    
    if (hasHeader()) {
      setCurrentCaption(getHeader().getCaption());
      getHeader().setCaption(newRowCaption);
    }
  }

  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    if (isAdding() || !isEnabled()) {
      return;
    }

    if (event.getTypeInt() == Event.ONMOUSEWHEEL) {
      int y = event.getMouseWheelVelocityY();

      HasDataTable display = getContent().getGrid();
      ScrollPager scroller = getScroller();

      if (y == 0 || display == null || scroller == null) {
        return;
      }
      if (display.isEditing()) {
        return;
      }

      Element elem = scroller.getElement();
      EventTarget target = event.getEventTarget();
      if (target != null && elem.isOrHasChild(Node.as(target))) {
        return;
      }
      if (EventUtils.isInputElement(target)) {
        EventUtils.eatEvent(event);
        return;
      }
      EventUtils.eatEvent(event);

      int rc = display.getRowCount();
      int start = display.getPageStart();
      int length = display.getPageSize();

      if (length > 0 && rc > length) {
        int p = -1;
        if (y > 0 && start + length < rc) {
          p = start + 1;
        } else if (y < 0 && start > 0) {
          p = start - 1;
        }

        if (p >= 0) {
          display.setPageStart(p, true, true);
        }
      }
    }
  }

  public void onEditForm(EditFormEvent event) {
    if (event.isOpening()) {
      showHeaderFooterScroller(false);
    } else if (event.isClosing()) {
      showHeaderFooterScroller(true);
    }
  }

  @Override
  public void onResize() {
    if (isAttached()) {
      super.onResize();
      getContent().getGrid().updatePageSize();
    }
  }

  public void setEnabled(boolean enabled) {
    if (enabled == isEnabled()) {
      return;
    }
    this.enabled = enabled;
    DomUtils.enableChildren(this, enabled);
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
    for (Widget widget : getChildren()) {
      if (widget instanceof View) {
        ((View) widget).setViewPresenter(viewPresenter);
      }
    }
  }
  
  public void showHeaderFooterScroller(boolean show) {
    if (hasHeader()) {
      getHeader().setEnabled(show);
    }
    if (hasFooter()) {
      setDirectionSize(getFooterDirection(), show ? getFooterHeight() : 0);
    }
    if (hasScroller()) {
      setDirectionSize(getScrollerDirection(), show ? getScrollerWidth() : 0);
    }
  }
  
  @Override
  protected void onLoad() {
    super.onLoad();
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      public void execute() {
        CellGrid grid = getContent().getGrid();
        if (!hasPaging()) {
          grid.refresh();
          return;
        }

        Collection<PagerView> pagers = getPagers();
        if (pagers != null) {
          for (PagerView pager : pagers) {
            pager.start(grid);
          }
        }
        
        int ps = Math.min(estimatePageSize(), grid.getRowCount());
        grid.setPageSize(ps, true, false);

        int ds = grid.getDataSize();
        if (ps > 0 && ps < ds) {
          grid.getRowData().subList(ps, ds).clear();
          grid.refresh();
        } else if (ps > 0 && ps > ds) {
          DataRequestEvent.fire(grid);
        } else {
          grid.refresh();
        }
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

  private int estimatePageSize() {
    if (hasPaging()) {
      int w = getElement().getClientWidth();
      int h = getElement().getClientHeight();

      if (w <= 0) {
        w = DomUtils.getParentClientWidth(this);
      }
      if (h <= 0) {
        h = DomUtils.getParentClientHeight(this);
      }

      return estimatePageSize(getContent(), w, h);
    }
    return BeeConst.UNDEF;
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
    return BeeConst.UNDEF;
  }

  private String getCurrentCaption() {
    return currentCaption;
  }

  private Direction getFooterDirection() {
    return footerDirection;
  }

  private Direction getHeaderDirection() {
    return headerDirection;
  }

  private Evaluator getRowMessage() {
    return rowMessage;
  }

  private Direction getScrollerDirection() {
    return scrollerDirection;
  }

  private boolean hasPaging() {
    return hasPaging;
  }

  private boolean hasSearch() {
    return hasSearch;
  }

  private boolean isAdding() {
    return adding;
  }

  private void setAdding(boolean adding) {
    this.adding = adding;
  }

  private void setCurrentCaption(String currentCaption) {
    this.currentCaption = currentCaption;
  }

  private void setFooterDirection(Direction footerDirection) {
    this.footerDirection = footerDirection;
  }

  private void setHasPaging(boolean hasPaging) {
    this.hasPaging = hasPaging;
  }

  private void setHasSearch(boolean hasSearch) {
    this.hasSearch = hasSearch;
  }

  private void setHeaderDirection(Direction headerDirection) {
    this.headerDirection = headerDirection;
  }

  private void setRowMessage(Evaluator rowMessage) {
    this.rowMessage = rowMessage;
  }

  private void setScrollerDirection(Direction scrollerDirection) {
    this.scrollerDirection = scrollerDirection;
  }
}
