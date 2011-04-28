package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;

import com.butent.bee.client.view.event.SortEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsRow;

import java.util.List;

public abstract class Provider implements SortEvent.Handler {

  private final HasDataTable display;
  
  private final List<HandlerRegistration> handlerRegistry = Lists.newArrayList();

  private boolean rangeChangeEnabled = true;

  protected Provider(HasDataTable display) {
    Assert.notNull(display);
    this.display = display;

    this.handlerRegistry.add(display.addRangeChangeHandler(new RangeChangeEvent.Handler() {
      public void onRangeChange(RangeChangeEvent event) {
        if (rangeChangeEnabled) {
          onRangeChanged();
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
  
  public boolean isRangeChangeEnabled() {
    return rangeChangeEnabled;
  }

  public abstract void onSort(SortEvent event);
  
  public void onUnload() {
    for (HandlerRegistration entry : handlerRegistry) {
      if (entry != null) {
        entry.removeHandler();
      }
    }
  }

  public void setRangeChangeEnabled(boolean rangeChangeEnabled) {
    this.rangeChangeEnabled = rangeChangeEnabled;
  }

  protected HasData<IsRow> getDisplay() {
    return display;
  }

  protected int getPageSize() {
    return getDisplay().getVisibleRange().getLength();
  }
  
  protected Range getRange() {
    return getDisplay().getVisibleRange();
  }
  
  protected void goTop(boolean forceRangeChange) {
    getDisplay().setVisibleRangeAndClearData(new Range(0, getPageSize()), forceRangeChange);
  }

  protected abstract void onRangeChanged();
}
