package com.butent.bee.client.view.navigation;

import com.google.common.base.Objects;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.logical.ScopeChangeEvent;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.ui.NavigationOrigin;

/**
 * Is an abstract class with default pager implementation behavior.
 */

public abstract class AbstractPager extends Composite implements PagerView {

  private HasDataTable display;

  private HandlerRegistration changeHandler;

  private Presenter viewPresenter = null;

  private boolean enabled = true;

  public HasDataTable getDisplay() {
    return display;
  }

  public int getPageSize() {
    return (getDisplay() == null) ? BeeConst.UNDEF : getDisplay().getPageSize();
  }

  public int getPageStart() {
    return (getDisplay() == null) ? BeeConst.UNDEF : getDisplay().getPageStart();
  }

  @Override
  public Element getPrintElement() {
    return getWidget().getElement();
  }

  public int getRowCount() {
    return (getDisplay() == null) ? BeeConst.UNDEF : getDisplay().getRowCount();
  }

  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  public String getWidgetId() {
    return DomUtils.getId(getWidget());
  }

  public boolean isEnabled() {
    return enabled;
  }

  public abstract void onScopeChange(ScopeChangeEvent event);

  public void setDisplay(HasDataTable display) {
    if (changeHandler != null) {
      changeHandler.removeHandler();
      changeHandler = null;
    }

    this.display = display;

    if (display != null) {
      changeHandler = display.addScopeChangeHandler(this);
    }
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void setViewPresenter(Presenter viewPresenter) {
    this.viewPresenter = viewPresenter;
  }

  public void start(HasDataTable displ) {
    if (!Objects.equal(displ, getDisplay())) {
      setDisplay(displ);
    }
  }
  
  protected abstract NavigationOrigin getNavigationOrigin();

  protected int getPage() {
    int pageSize = getPageSize();
    if (pageSize <= 0) {
      return BeeConst.UNDEF;
    }
    return (getPageStart() + pageSize - 1) / pageSize;
  }

  protected int getPageCount() {
    int pageSize = getPageSize();
    if (pageSize <= 0) {
      return BeeConst.UNDEF;
    }
    return (getRowCount() + pageSize - 1) / pageSize;
  }

  protected boolean hasNextPage() {
    if (getPageSize() <= 0) {
      return false;
    }
    return getPageStart() + getPageSize() < getRowCount();
  }

  protected boolean hasPreviousPage() {
    return getPageStart() > 0 && getRowCount() > 0;
  }

  protected void nextPage() {
    if (getPageSize() > 0) {
      setPageStart(getPageStart() + getPageSize());
    }
  }

  protected void previousPage() {
    if (getPageSize() > 0 && getPageStart() > 0) {
      setPageStart(getPageStart() - getPageSize());
    }
  }

  protected void setPageStart(int index) {
    if (getRowCount() > 0) {
      int start;
      if (getPageSize() > 0) {
        start = Math.min(index, getRowCount() - getPageSize());
      } else {
        start = Math.min(index,  getRowCount() - 1);
      }

      if (start != getPageStart()) {
        getDisplay().setPageStart(Math.max(0, start), true, true, getNavigationOrigin());
      }
    }
  }
}
