package com.butent.bee.client.data;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

import com.butent.bee.client.widget.InputSpinner;
import com.butent.bee.shared.utils.BeeUtils;

public class PageResizer extends AbstractPager {
  public static int minPageSize = 2;
  public static int maxPageSize = 100;
  public static int defaultStep = 1;

  public PageResizer(int value) {
    this(BeeUtils.limit(value, minPageSize, maxPageSize), minPageSize, maxPageSize, defaultStep);
  }
  
  public PageResizer(int value, int min, int max, int step) {
    InputSpinner widget = new InputSpinner(value, min, max, step);
    initWidget(widget);

    widget.addChangeHandler(new ChangeHandler() {
      public void onChange(ChangeEvent event) {
        HasRows display = getDisplay();
        int pageSize = getValue();
        if (display != null && pageSize > 0) {
          Range range = display.getVisibleRange();
          display.setVisibleRange(range.getStart(), pageSize);
        }
      }
    });
  }

  @Override
  protected void onRangeOrRowCountChanged() {
    int pageSize = getPageSize();
    if (pageSize > 0 && pageSize != getValue()) {
      getInputWidget().setValue(pageSize);
      if (pageSize < minPageSize) {
        getInputWidget().setMinValue(pageSize);
      }
      if (pageSize > maxPageSize) {
        getInputWidget().setMaxValue(pageSize);
      }
    }
    
    int rowCount = getDisplay().getRowCount();
    if (rowCount > 0 && rowCount <= maxPageSize && rowCount >= pageSize) {
      getInputWidget().setMaxValue(rowCount);
    }
  }
  
  private InputSpinner getInputWidget() {
    return (InputSpinner) getWidget();
  }
  
  private int getValue() {
    return getInputWidget().getIntValue();
  }
}
