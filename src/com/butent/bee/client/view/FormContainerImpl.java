package com.butent.bee.client.view;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.view.add.AddEndEvent;
import com.butent.bee.client.view.add.AddStartEvent;
import com.butent.bee.client.view.form.FormImpl;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.navigation.PagerView;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.event.ActiveRowChangeEvent;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

public class FormContainerImpl extends Split implements FormContainerView, HasNavigation,
    HasSearch, ActiveRowChangeEvent.Handler, AddStartEvent.Handler, AddEndEvent.Handler {

  public static String newRowCaption = "New Row";

  private Presenter viewPresenter = null;

  private Direction footerDirection = null;
  private Direction headerDirection = null;

  private int headerHeight = 22;
  private int footerHeight = 32;

  private boolean hasSearch = false;

  private Evaluator rowMessage = null;
  
  private boolean enabled = true;
  
  private String currentCaption = null;
  
  public FormContainerImpl() {
    this(-1);
  }

  public FormContainerImpl(int splitterSize) {
    super(splitterSize);
  }

  public FormContainerImpl(String style) {
    this(style, -1);
  }

  public FormContainerImpl(String style, int splitterSize) {
    super(style, splitterSize);
  }

  public void bind() {
    if (hasHeader()) {
      getContent().getDisplay().addLoadingStateChangeHandler(getHeader());
    }
    if (hasFooter()) {
      getContent().getDisplay().addSelectionCountChangeHandler(getFooter());
    }

    if (getRowMessage() != null) {
      getContent().getDisplay().addActiveRowChangeHandler(this);
    }
    
    getContent().addAddStartHandler(this);
    getContent().addAddEndHandler(this);
  }

  public void create(FormDescription formDescription, List<BeeColumn> dataColumns, int rowCount,
      BeeRowSet rowSet) {
    Assert.notNull(formDescription);

    setHasSearch(rowCount >= formDescription.getSearchThreshold());

    DataHeaderView header = new DataHeaderImpl();
    header.create(formDescription.getCaption(), formDescription.isReadOnly());
    
    boolean hasView = !BeeUtils.isEmpty(dataColumns);

    FormView content = new FormImpl();
    content.create(formDescription, dataColumns, rowCount, rowSet);

    DataFooterView footer;
    if (hasView) {
      footer = new DataFooterImpl();
      footer.create(rowCount, BeeConst.UNDEF, true, hasSearch());
    } else {
      footer = null;
    }

    addNorth(header.asWidget(), getHeaderHeight());
    setHeaderDirection(Direction.NORTH);

    if (footer != null) {
      addSouth(footer.asWidget(), getFooterHeight());
      setFooterDirection(Direction.SOUTH);
    }

    add(content.asWidget(), ScrollBars.BOTH);

    if (hasView) {
      Calculation rmc = formDescription.getRowMessage();
      if (rmc != null) {
        setRowMessage(Evaluator.create(rmc, null, dataColumns));
      }
    }
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "form-container");
  }

  public FormView getContent() {
    if (getCenter() == null) {
      return null;
    }
    return (FormView) getCenter();
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

  public Collection<PagerView> getPagers() {
    return ViewHelper.getPagers(this);
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
    if (hasFooter()) {
      setDirectionSize(getFooterDirection(), getFooterHeight());
    }
    
    setEnabled(true);
  }

  public void onAddStart(AddStartEvent event) {
    setEnabled(false);
    
    if (hasHeader()) {
      setCurrentCaption(getHeader().getCaption());
      getHeader().setCaption(newRowCaption);
    }
    if (hasFooter()) {
      setDirectionSize(getFooterDirection(), 0);
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

  public void setViewPresenter(Presenter viewPresenter) {
    this.viewPresenter = viewPresenter;
    for (Widget child : getChildren()) {
      if (child instanceof View) {
        ((View) child).setViewPresenter(viewPresenter);
      }
    }
  }

  @Override
  protected void onUnload() {
    if (getViewPresenter() != null) {
      getViewPresenter().onViewUnload();
    }
    super.onUnload();
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

  private boolean hasSearch() {
    return hasSearch;
  }

  private void setCurrentCaption(String currentCaption) {
    this.currentCaption = currentCaption;
  }

  private void setFooterDirection(Direction footerDirection) {
    this.footerDirection = footerDirection;
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
}
