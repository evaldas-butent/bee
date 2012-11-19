package com.butent.bee.client.view;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.Place;
import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.ActiveRowChangeEvent;
import com.butent.bee.client.event.logical.DataRequestEvent;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.ui.WidgetCreationCallback;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.view.add.AddEndEvent;
import com.butent.bee.client.view.add.AddStartEvent;
import com.butent.bee.client.view.edit.EditFormEvent;
import com.butent.bee.client.view.edit.HasEditState;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.CellGridImpl;
import com.butent.bee.client.view.grid.ExtWidget;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.navigation.PagerView;
import com.butent.bee.client.view.navigation.ScrollPager;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.NavigationOrigin;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Implements design content for a grid container component.
 */

public class GridContainerImpl extends Split implements GridContainerView, HasNavigation,
    HasSearch, ActiveRowChangeEvent.Handler, AddStartEvent.Handler, AddEndEvent.Handler,
    EditFormEvent.Handler, HasEditState {

  private Presenter viewPresenter = null;
  
  private String gridName = null;

  private String footerId = null;
  private String headerId = null;
  private String scrollerId = null;

  private int scrollerWidth = DomUtils.getScrollBarWidth() + 1;

  private boolean hasPaging = false;
  private boolean hasSearch = false;

  private Evaluator rowMessage = null;

  private boolean editing = false;
  private boolean enabled = true;

  private final List<ExtWidget> extWidgets = Lists.newArrayList();
  private WidgetCreationCallback extCreation = null;

  private IsRow lastRow = null;
  private boolean lastEnabled = false;

  private List<String> favorite = Lists.newArrayList();

  public GridContainerImpl() {
    super(-1);
    addStyleName("bee-GridContainer");
  }

  @Override
  public void bind() {
    if (hasFooter()) {
      getGridView().getGrid().addSelectionCountChangeHandler(getFooter());
    }

    getGridView().getGrid().addActiveRowChangeHandler(this);

    getGridView().addAddStartHandler(this);
    getGridView().addAddEndHandler(this);

    getGridView().addEditFormHandler(this);
  }

  @Override
  public void create(GridDescription gridDescription, List<BeeColumn> dataColumns, String relColumn,
      int rowCount, BeeRowSet rowSet, Order order, GridInterceptor gridInterceptor,
      Collection<UiOption> uiOptions, GridFactory.GridOptions gridOptions) {

    int minRows = BeeUtils.unbox(gridDescription.getPagingThreshold());
    setHasPaging(UiOption.hasPaging(uiOptions) && rowCount >= minRows);

    minRows = BeeUtils.unbox(gridDescription.getSearchThreshold());
    setHasSearch(UiOption.hasSearch(uiOptions) && rowCount >= minRows);

    boolean readOnly = BeeUtils.isTrue(gridDescription.isReadOnly());

    HeaderView header;
    if (gridDescription.hasGridHeader()) {
      header = GWT.create(HeaderImpl.class);

      String caption = (gridInterceptor == null) ? null : gridInterceptor.getCaption();
      if (caption == null) {
        caption = (gridOptions == null) ? null : gridOptions.getCaption();
      }
      if (caption == null) {
        caption = gridDescription.getCaption();
      }

      Set<Action> enabledActions = Sets.newHashSet(gridDescription.getEnabledActions());
      Set<Action> disabledActions = Sets.newHashSet(gridDescription.getDisabledActions());

      boolean fav = !BeeUtils.isEmpty(gridDescription.getFavorite());
      if (fav) {
        setFavorite(NameUtils.toList(gridDescription.getFavorite()));
      }

      if (enabledActions.contains(Action.BOOKMARK) != fav) {
        if (fav) {
          enabledActions.add(Action.BOOKMARK);
        } else {
          enabledActions.remove(Action.BOOKMARK);
        }
      }

      int min = BeeUtils.unbox(gridDescription.getMinNumberOfRows());
      if (min > 0 && rowCount <= min) {
        disabledActions.add(Action.DELETE);
      }
      int max = BeeUtils.unbox(gridDescription.getMaxNumberOfRows());
      if (max > 0 && rowCount >= max) {
        disabledActions.add(Action.ADD);
      }

      header.create(caption, !BeeUtils.isEmpty(gridDescription.getViewName()), readOnly, uiOptions,
          enabledActions, disabledActions);
    } else {
      header = null;
    }

    String name = gridDescription.getName();
    setGridName(name);

    GridView content = new CellGridImpl(name, gridDescription.getViewName(), relColumn);
    content.create(dataColumns, rowCount, rowSet, gridDescription, gridInterceptor, hasSearch(),
        order);

    FooterView footer;
    ScrollPager scroller;

    if (hasPaging() || hasSearch()) {
      footer = new FooterImpl();
      footer.create(rowCount, hasPaging(), true, hasSearch());
    } else {
      footer = null;
    }

    if (hasPaging()) {
      scroller = new ScrollPager();
    } else {
      scroller = null;
    }

    getExtWidgets().clear();
    if (gridDescription.hasWidgets()) {
      if (getExtCreation() == null) {
        setExtCreation(new WidgetCreationCallback());
      }

      for (String xml : gridDescription.getWidgets()) {
        ExtWidget extWidget = ExtWidget.create(xml, gridDescription.getViewName(), dataColumns,
            getExtCreation(), gridInterceptor);
        if (extWidget != null) {
          getExtWidgets().add(extWidget);
        }
      }
    }

    addExtWidgets(ExtWidget.Component.HEADER);
    if (header != null) {
      addNorth(header, header.getHeight());
      setHeaderId(header.getWidgetId());
    }

    addExtWidgets(ExtWidget.Component.FOOTER);
    if (footer != null) {
      addSouth(footer, footer.getHeight());
      setFooterId(footer.getWidgetId());
    }

    addExtWidgets(null);
    if (scroller != null) {
      addEast(scroller, getScrollerWidth());
      setScrollerId(scroller.getWidgetId());
      add(content, ScrollBars.HORIZONTAL);
      sinkEvents(Event.ONMOUSEWHEEL);
    } else {
      add(content, ScrollBars.BOTH);
    }

    if (gridDescription.getRowMessage() != null) {
      setRowMessage(Evaluator.create(gridDescription.getRowMessage(), null, dataColumns));
    }

    if (getExtCreation() != null) {
      getExtCreation().addBinding(name, getId(), gridDescription.getParent());
      getExtCreation().bind(this, getId());
    }
  }

  @Override
  public String getCaption() {
    return hasHeader() ? getHeader().getCaption() : null;
  }
  
  @Override
  public List<String> getFavorite() {
    return favorite;
  }

  @Override
  public GridView getGridView() {
    if (getCenter() == null) {
      return null;
    }
    return (GridView) getCenter();
  }

  @Override
  public HeaderView getHeader() {
    if (BeeUtils.isEmpty(getHeaderId())) {
      return null;
    }
    for (Widget widget : getChildren()) {
      if (widget instanceof HeaderView
          && BeeUtils.same(widget.getElement().getId(), getHeaderId())) {
        return (HeaderView) widget;
      }
    }
    return null;
  }

  @Override
  public String getIdPrefix() {
    return "grid-container";
  }

  @Override
  public Collection<PagerView> getPagers() {
    if (hasPaging()) {
      return ViewHelper.getPagers(this);
    } else {
      return null;
    }
  }

  @Override
  public Element getPrintElement() {
    return getElement();
  }

  @Override
  public Collection<SearchView> getSearchers() {
    if (hasSearch()) {
      return ViewHelper.getSearchers(this);
    } else {
      return null;
    }
  }

  @Override
  public String getSupplierKey() {
    return GridFactory.getSupplierKey(getGridName(), getGridView().getGridInterceptor());
  }

  @Override
  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  @Override
  public String getWidgetId() {
    return getId();
  }

  @Override
  public boolean isEditing() {
    return editing;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void onActiveRowChange(ActiveRowChangeEvent event) {
    IsRow rowValue = event.getRowValue();
    GridView gridView = getGridView();

    boolean rowEnabled;
    if (rowValue == null) {
      rowEnabled = false;
    } else {
      rowEnabled = !gridView.isReadOnly() && isEnabled() && gridView.isEnabled()
          && gridView.isRowEditable(rowValue, false);
    }

    if (DataUtils.sameIdAndVersion(rowValue, getLastRow()) && rowEnabled == wasLastEnabled()) {
      return;
    }

    if (getRowMessage() != null) {
      getRowMessage().update(event.getRowValue());
      String message = getRowMessage().evaluate();

      if (hasHeader()) {
        getHeader().setMessage(message);
      }
    }

    String eventSource = BeeUtils.notEmpty(getViewPresenter().getEventSource(), getId());
    BeeKeeper.getBus().fireEventFromSource(new ParentRowEvent(gridView.getViewName(), rowValue,
        rowEnabled), eventSource);

    setLastRow(rowValue);
    setLastEnabled(rowEnabled);
  }

  @Override
  public void onAddEnd(AddEndEvent event) {
    if (!event.isPopup()) {
      showChildren(true);
    }
    setEditing(false);
  }

  @Override
  public void onAddStart(AddStartEvent event) {
    setEditing(true);
    if (!event.isPopup()) {
      showChildren(false);
    }
  }

  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    if (isEditing() || !isEnabled()) {
      return;
    }

    if (event.getTypeInt() == Event.ONMOUSEWHEEL) {
      int y = event.getMouseWheelVelocityY();

      HasDataTable display = getGridView().getGrid();
      ScrollPager scroller = getScroller();

      if (y == 0 || display == null || scroller == null) {
        return;
      }
      if (display.isEditing()) {
        return;
      }

      EventTarget target = event.getEventTarget();
      if (target != null && scroller.getElement().isOrHasChild(Node.as(target))) {
        return;
      }

      event.preventDefault();
      if (EventUtils.isInputElement(target)) {
        return;
      }

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
          display.setPageStart(p, true, true, NavigationOrigin.MOUSE);
        }
      }
    }
  }

  @Override
  public void onEditForm(EditFormEvent event) {
    if (event.isOpening()) {
      setEditing(true);
      if (!event.isPopup()) {
        showChildren(false);
      }

    } else if (event.isClosing()) {
      if (!event.isPopup()) {
        showChildren(true);
      }
      setEditing(false);
    }
  }

  @Override
  public boolean onHistory(Place place, boolean forward) {
    return getGridView().onHistory(place, forward);
  }

  @Override
  public boolean onPrint(Element source, Element target) {
    boolean ok;
    
    if (getGridView().getGrid().getId().equals(source.getId())) {
      NodeList<Element> children = DomUtils.getChildren(target);
      List<Element> hide = Lists.newArrayList();

      for (int i = 0; i < children.getLength(); i++) {
        Element cell = children.getItem(i);
        
        if (getGridView().getGrid().isFooterCell(cell)) {
          boolean show = false;

          String col = DomUtils.getDataColumn(cell);
          if (BeeUtils.isDigit(col)) {
            ColumnFooter columnFooter = getGridView().getGrid().getFooter(BeeUtils.toInt(col));
            if (columnFooter != null && !BeeUtils.isEmpty(columnFooter.getValue())) {
              show = true;
            }
          }
          
          if (!show) {
            StyleUtils.setHeight(cell, 0);
            StyleUtils.hideScroll(cell);
          }
        }
      }
      
      for (Element child : hide) {
        StyleUtils.hideDisplay(child);
      }
      ok = true;
    
    } else if (getGridView().getGrid().getElement().isOrHasChild(source)) {
      ok = true;

    } else if (getId().equals(source.getId())) {
      int width = source.getClientWidth();
      int height = source.getClientHeight();
      
      Element content = getGridView().getGrid().getElement();
      int delta = content.getScrollWidth() - content.getClientWidth();
      if (delta > 0) {
        width += delta;
      }
      delta = content.getScrollHeight() - content.getClientHeight();
      if (delta > 0) {
        height += delta;
      }

      StyleUtils.setSize(target, width, height);
      ok = true;

    } else if (hasHeader() && getHeader().asWidget().getElement().isOrHasChild(source)) {
      ok = getHeader().onPrint(source, target);
    
    } else if (hasFooter() && getFooter().asWidget().getElement().isOrHasChild(source)) {
      ok = getFooter().onPrint(source, target);

    } else if (hasScroller() && getScroller().asWidget().getElement().isOrHasChild(source)) {
      ok = getScroller().onPrint(source, target);
      
    } else {
      ok = true;
    }
    return ok;
  }

  @Override
  public void onResize() {
    if (isAttached() && providesResize()) {
      super.onResize();
      getGridView().getGrid().updatePageSize();
    }
  }

  @Override
  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (enabled == isEnabled()) {
      return;
    }
    this.enabled = enabled;
    DomUtils.enableChildren(this, enabled);
  }

  public void setFavorite(List<String> favorite) {
    BeeUtils.overwrite(this.favorite, favorite);
  }

  @Override
  public void setViewPresenter(Presenter viewPresenter) {
    this.viewPresenter = viewPresenter;
    for (Widget widget : getChildren()) {
      if (widget instanceof View && ((View) widget).getViewPresenter() == null) {
        ((View) widget).setViewPresenter(viewPresenter);
      }
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      public void execute() {
        CellGrid grid = getGridView().getGrid();
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

        int ps = estimatePageSize();
        grid.setPageSize(ps, true);

        int ds = grid.getDataSize();
        if (ps > 0 && ps < ds) {
          grid.getRowData().subList(ps, ds).clear();
          grid.refresh();
        } else if (ps > 0 && ps > ds && ds < grid.getRowCount()) {
          DataRequestEvent.fire(grid, NavigationOrigin.SYSTEM);
        } else {
          grid.refresh();
        }
      }
    });
  }

  @Override
  protected void onUnload() {
    if (!Global.isTemporaryDetach() && getViewPresenter() != null) {
      getViewPresenter().onViewUnload();
    }
    super.onUnload();
  }

  private void addExtWidgets(ExtWidget.Component before) {
    if (getExtWidgets().isEmpty()) {
      return;
    }

    boolean head = ExtWidget.Component.HEADER.equals(before);
    boolean foot = ExtWidget.Component.FOOTER.equals(before);

    boolean ok;
    for (ExtWidget extWidget : getExtWidgets()) {
      if (extWidget.precedesHeader()) {
        ok = head;
      } else if (extWidget.precedesFooter()) {
        ok = foot;
      } else {
        ok = !head && !foot;
      }

      if (ok) {
        add(extWidget.getWidget(), extWidget.getDirection(), extWidget.getSize(),
            extWidget.getScrollBars(), extWidget.getSplSize());
      }
    }
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

      return estimatePageSize(getGridView(), w, h);
    }
    return BeeConst.UNDEF;
  }

  private int estimatePageSize(GridView content, int containerWidth, int containerHeight) {
    if (content != null && containerHeight > 0) {
      int w = containerWidth;
      int h = containerHeight;

      if (hasHeader()) {
        h -= getHeader().getHeight();
      }
      if (hasFooter()) {
        h -= getFooter().getHeight();
      }
      if (hasScroller()) {
        w -= getScrollerWidth();
      }

      for (ExtWidget extWidget : getExtWidgets()) {
        if (extWidget.getDirection().isHorizontal()) {
          w -= extWidget.getTotalSize();
        } else {
          h -= extWidget.getTotalSize();
        }
      }

      return content.estimatePageSize(w, h);
    }
    return BeeConst.UNDEF;
  }

  private WidgetCreationCallback getExtCreation() {
    return extCreation;
  }

  private List<ExtWidget> getExtWidgets() {
    return extWidgets;
  }

  private FooterView getFooter() {
    if (BeeUtils.isEmpty(getFooterId())) {
      return null;
    }
    for (Widget widget : getChildren()) {
      if (widget instanceof FooterView
          && BeeUtils.same(widget.getElement().getId(), getFooterId())) {
        return (FooterView) widget;
      }
    }
    return null;
  }

  private String getFooterId() {
    return footerId;
  }

  private String getGridName() {
    return gridName;
  }

  private String getHeaderId() {
    return headerId;
  }

  private IsRow getLastRow() {
    return lastRow;
  }

  private Evaluator getRowMessage() {
    return rowMessage;
  }

  private ScrollPager getScroller() {
    if (BeeUtils.isEmpty(getScrollerId())) {
      return null;
    }
    for (Widget widget : getChildren()) {
      if (widget instanceof ScrollPager
          && BeeUtils.same(widget.getElement().getId(), getScrollerId())) {
        return (ScrollPager) widget;
      }
    }
    return null;
  }

  private String getScrollerId() {
    return scrollerId;
  }

  private int getScrollerWidth() {
    return scrollerWidth;
  }

  private boolean hasFooter() {
    return !BeeUtils.isEmpty(getFooterId());
  }

  private boolean hasHeader() {
    return !BeeUtils.isEmpty(getHeaderId());
  }

  private boolean hasPaging() {
    return hasPaging;
  }

  private boolean hasScroller() {
    return !BeeUtils.isEmpty(getScrollerId());
  }

  private boolean hasSearch() {
    return hasSearch;
  }

  private void setExtCreation(WidgetCreationCallback extCreation) {
    this.extCreation = extCreation;
  }

  private void setFooterId(String footerId) {
    this.footerId = footerId;
  }

  private void setGridName(String gridName) {
    this.gridName = gridName;
  }

  private void setHasPaging(boolean hasPaging) {
    this.hasPaging = hasPaging;
  }

  private void setHasSearch(boolean hasSearch) {
    this.hasSearch = hasSearch;
  }

  private void setHeaderId(String headerId) {
    this.headerId = headerId;
  }

  private void setLastEnabled(boolean lastEnabled) {
    this.lastEnabled = lastEnabled;
  }
  
  private void setLastRow(IsRow lastRow) {
    this.lastRow = lastRow;
  }

  private void setRowMessage(Evaluator rowMessage) {
    this.rowMessage = rowMessage;
  }

  private void setScrollerId(String scrollerId) {
    this.scrollerId = scrollerId;
  }

  private void showChildren(boolean show) {
    if (!show) {
      setProvidesResize(false);
    }

    if (hasHeader()) {
      HeaderView header = getHeader();
      if (header != null) {
        setWidgetSize(header.asWidget(), show ? header.getHeight() : 0);
      }
    }
    if (hasFooter()) {
      FooterView footer = getFooter();
      if (footer != null) {
        setWidgetSize(footer.asWidget(), show ? footer.getHeight() : 0);
      }
    }
    if (hasScroller()) {
      ScrollPager scroller = getScroller();
      if (scroller != null) {
        setWidgetSize(scroller, show ? getScrollerWidth() : 0);
      }
    }

    for (ExtWidget extWidget : getExtWidgets()) {
      if (extWidget.isHidable()) {
        setWidgetSize(extWidget.getWidget().asWidget(), show ? extWidget.getSize() : 0);
      }
    }

    if (show) {
      setProvidesResize(true);
    }
  }

  private boolean wasLastEnabled() {
    return lastEnabled;
  }
}
