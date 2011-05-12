package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;

import com.butent.bee.client.view.event.SortEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;

import java.util.List;

/**
 * Enables to manage ranges of data shown in user interface tables.
 */

public abstract class Provider implements SortEvent.Handler {

  private final HasDataTable display;

  private final List<HandlerRegistration> handlerRegistry = Lists.newArrayList();

  private boolean rangeChangeEnabled = true;

  private Filter filter = null;
  private Order order = null;
  
  protected Provider(HasDataTable display) {
    Assert.notNull(display);
    this.display = display;

    this.handlerRegistry.add(display.addRangeChangeHandler(new RangeChangeEvent.Handler() {
      public void onRangeChange(RangeChangeEvent event) {
        if (rangeChangeEnabled) {
          onRangeChanged(false);
        }
      }
    }));

    this.handlerRegistry.add(display.addSortHandler(this));
  }

  public void disableRangeChange() {
    setRangeChangeEnabled(false);
  }

  public void enableRangeChange() {
    setRangeChangeEnabled(true);
  }

  public Filter getFilter() {
    return filter;
  }

  public Order getOrder() {
    return order;
  }

  public boolean isRangeChangeEnabled() {
    return rangeChangeEnabled;
  }
 
  public void onFilterChanged(Filter newFilter, int rowCount) {
    setFilter(newFilter);
    getDisplay().setRowCount(rowCount);
    goTop();
  }
  
  public abstract void onSort(SortEvent event);

  public void onUnload() {
    for (HandlerRegistration entry : handlerRegistry) {
      if (entry != null) {
        entry.removeHandler();
      }
    }
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public void setOrder(Order order) {
    this.order = order;
  }
  
  public void setRangeChangeEnabled(boolean rangeChangeEnabled) {
    this.rangeChangeEnabled = rangeChangeEnabled;
  }

  protected HasDataTable getDisplay() {
    return display;
  }

  protected int getPageSize() {
    return getDisplay().getVisibleRange().getLength();
  }

  protected Range getRange() {
    return getDisplay().getVisibleRange();
  }

  protected void goTop() {
    getDisplay().setPageStart(0);
    onRangeChanged(true);
  }

  protected abstract void onRangeChanged(boolean updateActiveRow);
}
