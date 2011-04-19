package com.butent.bee.client.data;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;

import com.butent.bee.shared.data.IsRow;

public abstract class Provider {

  private final HasData<IsRow> display;
  private final HandlerRegistration rangeChangeHandler;
  private boolean rangeChangeEnabled = true;

  protected Provider(HasData<IsRow> display) {
    this.display = display;
    this.rangeChangeHandler = display.addRangeChangeHandler(new RangeChangeEvent.Handler() {
      public void onRangeChange(RangeChangeEvent event) {
        if (rangeChangeEnabled) {
          onRangeChanged();
        }
      }
    });
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

  public void onUnload() {
    rangeChangeHandler.removeHandler();
  }

  public void setRangeChangeEnabled(boolean rangeChangeEnabled) {
    this.rangeChangeEnabled = rangeChangeEnabled;
  }

  protected HasData<IsRow> getDisplay() {
    return display;
  }

  protected Range getRange() {
    return display.getVisibleRange();
  }

  protected abstract void onRangeChanged();
}
