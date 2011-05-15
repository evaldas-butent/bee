package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.view.event.MultiDeleteEvent;
import com.butent.bee.client.view.event.RowDeleteEvent;
import com.butent.bee.client.view.event.SortEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.cache.CacheManager;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Enables to manage ranges of data shown in user interface tables.
 */

public abstract class Provider implements SortEvent.Handler, RowDeleteEvent.Handler, MultiDeleteEvent.Handler {

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
    
    this.handlerRegistry.add(RowDeleteEvent.register(this));
    this.handlerRegistry.add(MultiDeleteEvent.register(this));
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

  public void onMultiDelete(MultiDeleteEvent event) {
    if (BeeUtils.same(getViewName(), event.getViewName())) {
      refresh();
    }
  }

  public void onRowDelete(RowDeleteEvent event) {
    if (BeeUtils.same(getViewName(), event.getViewName())) {
      refresh();
    }
  }

  public abstract void onSort(SortEvent event);

  public void onUnload() {
    for (HandlerRegistration entry : handlerRegistry) {
      if (entry != null) {
        entry.removeHandler();
      }
    }
  }
  
  public void refresh() {
    CacheManager.removeQuietly(getViewName());

    final Filter flt = getFilter();
    Queries.getRowCount(getViewName(), flt, new Queries.IntCallback() {
      @Override
      public void onResponse(int value) {
        if (value <= 0) {
          BeeKeeper.getLog().warning(getViewName(), flt, "refresh: row count", value);
          if (flt == null) {
            return;
          }
          
          Queries.getRowCount(getViewName(), new Queries.IntCallback() {
            @Override
            public void onResponse(int rowCount) {
              if (rowCount <= 0) {
                BeeKeeper.getLog().warning(getViewName(), "refresh: row count", rowCount);
              } else {
                BeeKeeper.getLog().info(getViewName(), "filter off");
                setFilter(null);
                getDisplay().setRowCount(rowCount);
                onRefresh();
              }
            }
          });
        } else {
          getDisplay().setRowCount(value);
          onRefresh();
        }
      }
    });
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
  
  protected abstract String getViewName();

  protected void goTop() {
    getDisplay().setPageStart(0);
    onRangeChanged(true);
  }

  protected abstract void onRangeChanged(boolean updateActiveRow);

  protected abstract void onRefresh();
}
