package com.butent.bee.client.data;

import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

public class RangeInfo extends AbstractPager {
  private BeeLabel label = new BeeLabel();

  public RangeInfo() {
    initWidget(label);
  }

  @Override
  protected void onRangeOrRowCountChanged() {
    HasRows display = getDisplay();
    Assert.notNull(display);
    
    Range range = display.getVisibleRange();
    int start = range.getStart() + 1;
    int end = BeeUtils.min(start + range.getLength() - 1, display.getRowCount());
    label.setText(start + " - " + end + " / " + display.getRowCount());
  }
}
