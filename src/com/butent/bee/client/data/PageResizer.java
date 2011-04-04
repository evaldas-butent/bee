package com.butent.bee.client.data;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

import com.butent.bee.client.widget.BeeTextBox;
import com.butent.bee.client.widget.InputSpinner;
import com.butent.bee.shared.utils.BeeUtils;

public class PageResizer extends AbstractPager {
  private BeeTextBox widget;

  public PageResizer(int value, int min, int max, int step) {
    this.widget = new InputSpinner(value, min, max, step);
    initWidget(widget);

    widget.addChangeHandler(new ChangeHandler() {
      public void onChange(ChangeEvent event) {
        HasRows display = getDisplay();
        int pageSize = BeeUtils.toInt(widget.getValue());
        if (display != null && pageSize > 0) {
          Range range = display.getVisibleRange();
          display.setVisibleRange(range.getStart(), pageSize);
        }
      }
    });
  }

  @Override
  protected void onRangeOrRowCountChanged() {
  }
}
